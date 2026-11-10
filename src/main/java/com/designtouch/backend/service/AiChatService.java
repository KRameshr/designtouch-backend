package com.designtouch.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Service for generating AI responses for the DesignTouch+ customer support and lead generation chatbot.
 * Integrates with Google Gemini API (default) or OpenAI API via lightweight HTTP client.
 * Features automatic capacity failover, temporary circuit-breaking for congested models, and token-by-token streaming.
 */
@Service
@Slf4j
public class AiChatService {

    public static final String SYSTEM_PROMPT = """
            You are the official AI assistant for DesignTouch+, a premier creative studio specializing in video editing, motion graphics, and high-impact visual storytelling.

            AGENCY PROFILE:
            - Business: DesignTouch+
            - Founder: Rohit Sharma
            - Tagline: "A creative studio for visual storytelling, brand films, and high-impact content."
            - Direct Contact: designtouchplus@gmail.com, website contact form, and direct WhatsApp.

            CORE SERVICES:
            1. Video Editing (/services/video-editing): High-retention edits for YouTube, social media, ads, podcasts, and digital shows. Includes precision pacing, audio mastery, sound effects, B-roll, and color grading.
            2. Motion Graphics (/services/motion-graphics): 2D/3D animation, kinetic typography, explainer narratives, logo animations, and UI/UX motion.
            3. Social Media Content (/services/social-media-content): Scroll-stopping vertical videos, Instagram Reels, YouTube Shorts, and TikTok campaigns engineered to hook viewers within 3 seconds.
            4. YouTube Management (/services/youtube-management): Channel strategy, thumbnail optimization, retention analysis, video editing, metadata SEO, and upload scheduling.
            5. Graphic Design (/services/graphic-design): Social posts, digital banners, marketing collateral, posters, and campaign brand assets.
            6. YouTube Thumbnail Design (/services/thumbnail-design): High-CTR thumbnails with visual hierarchy, color theory, custom facial cutouts, and 3D lighting.
            7. Branding & Visual Identity (/services/branding): Logo design, brand guidelines, typography systems, and color palettes.
            8. Brand Films & Commercial Ads (/services/commercial-ads): Cinematic commercial productions and promotional films.

            FORMATTING & STYLE RULES:
            - Use clean, standard Markdown:
              - Use bold (**like this**) for service names and key terms.
              - Use bullet points (* Item) for listing features, services, or recommendations.
              - Use markdown links for website pages: [Video Editing](/services/video-editing), [Thumbnail Design](/services/thumbnail-design), [Pricing](/pricing), [Contact](/contact).
              - Keep paragraphs clean and well-spaced.
            - Keep responses concise (typically 2-4 sentences or short bullet points). Avoid overwhelming walls of text.
            - Never invent or promise specific prices, discounts, fixed delivery dates, or guaranteed view metrics. Direct visitors to [Pricing](/pricing) or the [Contact](/contact) form for custom quotes.
            - If a visitor asks about something unrelated, politely clarify our studio's creative focus.
            """;

    public static final String UNCONFIGURED_FALLBACK =
            "Thanks for reaching out! Our AI assistant is currently in configuration mode as no AI API key has been configured on the backend server (GEMINI_API_KEY). " +
            "In the meantime, our creative team is actively available! You can reach us directly at designtouchplus@gmail.com, or view our plans on our [Pricing](/pricing) page.";

    private final String provider;
    private final String geminiApiKey;
    private final String geminiModel;
    private final String openAiApiKey;
    private final String openAiModel;
    private final ObjectMapper objectMapper;
    private volatile HttpClient httpClient;

    // Temporary cooldown for models encountering 503 capacity errors or timeouts (60s TTL)
    private final Map<String, Long> modelDegradedUntil = new ConcurrentHashMap<>();

    @Autowired
    public AiChatService(
            @Value("${app.ai.provider:gemini}") String provider,
            @Value("${app.ai.gemini.api-key:}") String geminiApiKey,
            @Value("${app.ai.gemini.model:gemini-3.8-flash}") String geminiModel,
            @Value("${app.ai.openai.api-key:}") String openAiApiKey,
            @Value("${app.ai.openai.model:gpt-4o-mini}") String openAiModel,
            ObjectMapper objectMapper
    ) {
        this(provider, geminiApiKey, geminiModel, openAiApiKey, openAiModel, objectMapper, true);
    }

    public AiChatService(
            String provider,
            String geminiApiKey,
            String geminiModel,
            String openAiApiKey,
            String openAiModel,
            ObjectMapper objectMapper,
            boolean allowDotEnvFallback
    ) {
        this.provider = (provider != null && !provider.isBlank()) ? provider.trim().toLowerCase() : "gemini";

        String resolvedGeminiKey = sanitizeApiKey(geminiApiKey);
        if (resolvedGeminiKey.isBlank() && allowDotEnvFallback) {
            String envVal = System.getenv("GEMINI_API_KEY");
            if (envVal != null && !envVal.isBlank()) {
                resolvedGeminiKey = sanitizeApiKey(envVal);
            }
        }
        if (resolvedGeminiKey.isBlank() && allowDotEnvFallback) {
            resolvedGeminiKey = tryReadFromDotEnv("GEMINI_API_KEY");
        }
        this.geminiApiKey = resolvedGeminiKey;

        String resolvedModel = geminiModel;
        if ((resolvedModel == null || resolvedModel.isBlank()) && allowDotEnvFallback) {
            String envModel = System.getenv("GEMINI_MODEL");
            resolvedModel = (envModel != null && !envModel.isBlank()) ? envModel.trim() : "gemini-3.8-flash";
        }
        this.geminiModel = sanitizeGeminiModel(resolvedModel);

        String resolvedOpenAiKey = sanitizeApiKey(openAiApiKey);
        if (resolvedOpenAiKey.isBlank() && allowDotEnvFallback) {
            String envVal = System.getenv("OPENAI_API_KEY");
            if (envVal != null && !envVal.isBlank()) {
                resolvedOpenAiKey = sanitizeApiKey(envVal);
            }
        }
        if (resolvedOpenAiKey.isBlank() && allowDotEnvFallback) {
            resolvedOpenAiKey = tryReadFromDotEnv("OPENAI_API_KEY");
        }
        this.openAiApiKey = resolvedOpenAiKey;
        this.openAiModel = (openAiModel != null && !openAiModel.isBlank()) ? openAiModel.trim() : "gpt-4o-mini";

        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();

        if (isConfigured()) {
            log.info("AiChatService initialized with provider: {} and model: {} (API key configured: true)", this.provider, this.geminiModel);
        } else {
            log.info("AiChatService initialized in fallback mode: No {} API key configured", this.provider);
        }
    }

    public boolean isConfigured() {
        if ("openai".equalsIgnoreCase(provider)) {
            return !openAiApiKey.isBlank();
        }
        return !geminiApiKey.isBlank();
    }

    public String getProvider() {
        return provider;
    }

    public String getGeminiModel() {
        return geminiModel;
    }

    private HttpClient getOrCreateHttpClient() {
        if (httpClient == null) {
            synchronized (this) {
                if (httpClient == null) {
                    httpClient = HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_2)
                            .connectTimeout(Duration.ofSeconds(10))
                            .build();
                }
            }
        }
        return httpClient;
    }

    /**
     * Determines candidate models ordered by priority and health status.
     * If the configured model recently encountered a 503 or socket stall,
     * stable fallback models are prioritized during the 60s cooldown.
     */
    public List<String> getOrderedModelsToAttempt() {
        List<String> result = new ArrayList<>();
        long now = System.currentTimeMillis();
        boolean primaryDegraded = modelDegradedUntil.getOrDefault(geminiModel, 0L) > now;
        List<String> fallbacks = List.of("gemini-3.6-flash", "gemini-3.5-flash", "gemini-3.5-flash-lite");

        if (primaryDegraded) {
            log.info("Primary model '{}' temporarily degraded (re-evaluating in {}s). Prioritizing active fallback.",
                    geminiModel, Math.max(1, (modelDegradedUntil.get(geminiModel) - now) / 1000));
            for (String f : fallbacks) {
                if (!f.equalsIgnoreCase(geminiModel)) {
                    result.add(f);
                }
            }
            result.add(geminiModel);
        } else {
            result.add(geminiModel);
            for (String f : fallbacks) {
                if (!f.equalsIgnoreCase(geminiModel)) {
                    result.add(f);
                }
            }
        }
        return result;
    }

    private void markModelDegraded(String model) {
        modelDegradedUntil.put(model, System.currentTimeMillis() + 60_000L);
    }

    private void clearModelDegradation(String model) {
        modelDegradedUntil.remove(model);
    }

    /**
     * Generates a fresh conversational AI response for the current visitor message.
     * No previous conversation history is loaded or persisted.
     */
    public String generateResponse(String currentMessage, String visitorName) {
        if (!isConfigured()) {
            log.info("AI Chatbot invoked but no provider API key configured (provider: {}). Returning fallback message.", provider);
            return UNCONFIGURED_FALLBACK;
        }

        try {
            if ("openai".equalsIgnoreCase(provider)) {
                return callOpenAi(currentMessage, visitorName);
            } else {
                return callGemini(currentMessage, visitorName);
            }
        } catch (GeminiApiException e) {
            log.error("Gemini API call failed [status: {}, category: {}]: {}", e.getStatusCode(), e.getCategory(), e.getMessage());
            if (e.getStatusCode() == 429) {
                return "Our AI assistant is receiving unusually high traffic right now. Please wait a moment and try again, or reach out to us directly at designtouchplus@gmail.com.";
            } else if (e.getStatusCode() == 401 || e.getStatusCode() == 403) {
                return "Our AI assistant is currently updating credentials. In the meantime, our creative team is available directly at designtouchplus@gmail.com or via our [Contact](/contact) page.";
            } else if (e.getStatusCode() == 503) {
                return "Google's AI service is currently experiencing exceptionally high demand across its clusters. Please try your question again in a moment, or contact us directly at designtouchplus@gmail.com.";
            } else if (e.getStatusCode() == 404) {
                return "The requested AI model is currently not available on the upstream server. Please reach out to our team directly at designtouchplus@gmail.com.";
            } else if (e.getStatusCode() == 400) {
                return "The AI request could not be processed due to invalid parameters. Please submit your question again or contact us directly at designtouchplus@gmail.com.";
            }
            return "I apologize, but an unexpected error occurred while reaching the AI service (HTTP " + e.getStatusCode() + "). Please feel free to ask again or contact us directly at designtouchplus@gmail.com.";
        } catch (java.net.http.HttpTimeoutException e) {
            log.error("Gemini API call timed out after duration limit: {}", e.getMessage());
            return "The AI service response timed out due to high network latency. Please submit your question again or reach out to us at designtouchplus@gmail.com.";
        } catch (Exception e) {
            log.error("Failed to generate AI response via provider {}: {}", provider, e.getMessage(), e);
            return "I apologize, but I encountered a temporary connection issue reaching our AI service. Please feel free to ask again or contact us directly at designtouchplus@gmail.com.";
        }
    }

    /**
     * Backward-compatible overload ignoring any previous messages.
     */
    public String generateResponse(Object ignoredPreviousMessages, String currentMessage, String visitorName) {
        return generateResponse(currentMessage, visitorName);
    }

    /**
     * Streams AI tokens progressively via Gemini streamGenerateContent SSE endpoint.
     * Starts with a fresh AI context.
     */
    public String generateStreamingResponse(
            String currentMessage,
            String visitorName,
            Consumer<String> tokenConsumer
    ) {
        if (!isConfigured()) {
            log.info("AI Chatbot invoked but no provider API key configured (provider: {}).", provider);
            tokenConsumer.accept(UNCONFIGURED_FALLBACK);
            return UNCONFIGURED_FALLBACK;
        }

        if ("openai".equalsIgnoreCase(provider)) {
            String resp = generateResponse(currentMessage, visitorName);
            tokenConsumer.accept(resp);
            return resp;
        }

        try {
            return callGeminiStreaming(currentMessage, visitorName, tokenConsumer);
        } catch (Exception e) {
            log.warn("Gemini streaming failed ({}); falling back to standard generation.", e.getMessage());
            String fallback = generateResponse(currentMessage, visitorName);
            tokenConsumer.accept(fallback);
            return fallback;
        }
    }

    /**
     * Backward-compatible overload ignoring any previous messages.
     */
    public String generateStreamingResponse(
            Object ignoredPreviousMessages,
            String currentMessage,
            String visitorName,
            Consumer<String> tokenConsumer
    ) {
        return generateStreamingResponse(currentMessage, visitorName, tokenConsumer);
    }

    private String buildGeminiRequestBody(String cleanCurrent, String visitorName) throws Exception {
        String systemInstructionWithVisitor = SYSTEM_PROMPT + "\nNote: The visitor's name is " +
                (visitorName != null && !visitorName.isBlank() ? visitorName : "Visitor") + ".";

        List<Map<String, Object>> contents = List.of(
                Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", cleanCurrent))
                )
        );

        Map<String, Object> requestPayload = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemInstructionWithVisitor))
                ),
                "contents", contents,
                "generationConfig", Map.of(
                        "maxOutputTokens", 800
                )
        );

        return objectMapper.writeValueAsString(requestPayload);
    }

    private String callGemini(String currentMessage, String visitorName) throws Exception {
        String cleanCurrent = currentMessage != null ? currentMessage.trim() : "";
        if (cleanCurrent.isBlank()) {
            return "How can we assist you with your video editing or motion graphics project today?";
        }

        String jsonBody = buildGeminiRequestBody(cleanCurrent, visitorName);
        List<String> modelsToAttempt = getOrderedModelsToAttempt();

        int lastStatusCode = 0;
        String lastErrorMsg = "";
        String lastCategory = "UNKNOWN";

        for (int i = 0; i < modelsToAttempt.size(); i++) {
            String targetModel = modelsToAttempt.get(i);
            long startTime = System.currentTimeMillis();

            Duration attemptTimeout = targetModel.equalsIgnoreCase(geminiModel)
                    ? Duration.ofMillis(4500)
                    : Duration.ofSeconds(12);

            String endpoint = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    URLEncoder.encode(targetModel, StandardCharsets.UTF_8),
                    URLEncoder.encode(geminiApiKey, StandardCharsets.UTF_8)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", geminiApiKey)
                    .timeout(attemptTimeout)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response;
            try {
                response = getOrCreateHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception ex) {
                long durationMs = System.currentTimeMillis() - startTime;
                log.warn("Gemini network/socket error on model '{}' after {}ms: {}", targetModel, durationMs, ex.getMessage());
                markModelDegraded(targetModel);
                if (i < modelsToAttempt.size() - 1) {
                    continue;
                }
                throw ex;
            }

            int statusCode = response.statusCode();
            long latencyMs = System.currentTimeMillis() - startTime;

            if (statusCode >= 200 && statusCode < 300) {
                clearModelDegradation(targetModel);
                if (i > 0) {
                    log.info("Gemini automatic capacity failover succeeded with model '{}' [HTTP 200, latency: {}ms]", targetModel, latencyMs);
                } else {
                    log.info("Gemini API call succeeded with model '{}' [HTTP 200, latency: {}ms]", targetModel, latencyMs);
                }
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    JsonNode candidateNode = candidates.get(0);
                    String finishReason = candidateNode.path("finishReason").asText("");
                    if ("SAFETY".equalsIgnoreCase(finishReason)) {
                        log.warn("Gemini response blocked by safety filters on model '{}'.", targetModel);
                        return "I am here to assist with DesignTouch+ creative services and video editing projects. How can I help with your project goals?";
                    }
                    JsonNode parts = candidateNode.path("content").path("parts");
                    if (parts.isArray() && !parts.isEmpty()) {
                        String reply = parts.get(0).path("text").asText().trim();
                        if (!reply.isBlank()) {
                            return reply;
                        }
                    }
                }
                log.warn("Gemini response missing text parts on model '{}': {}", targetModel, response.body());
                return "Thank you for reaching out! How can we assist with your project today?";
            } else {
                lastStatusCode = statusCode;
                lastErrorMsg = extractGeminiErrorMessage(response.body());
                lastCategory = determineErrorCategory(statusCode, lastErrorMsg);

                log.warn("Gemini API call failed [model: '{}', status: {}, category: {}, latency: {}ms]: {}",
                        targetModel, statusCode, lastCategory, latencyMs, lastErrorMsg);

                if (statusCode == 503 || statusCode == 404) {
                    markModelDegraded(targetModel);
                    if (i < modelsToAttempt.size() - 1) {
                        log.warn("Model '{}' returned HTTP {} ({}). Attempting capacity failover to '{}'...",
                                targetModel, statusCode, lastCategory, modelsToAttempt.get(i + 1));
                        continue;
                    }
                }

                break;
            }
        }

        throw new GeminiApiException(lastStatusCode, lastErrorMsg, lastCategory);
    }

    private String callGeminiStreaming(
            String currentMessage,
            String visitorName,
            Consumer<String> tokenConsumer
    ) throws Exception {
        String cleanCurrent = currentMessage != null ? currentMessage.trim() : "";
        if (cleanCurrent.isBlank()) {
            String msg = "How can we assist you with your video editing or motion graphics project today?";
            tokenConsumer.accept(msg);
            return msg;
        }

        String jsonBody = buildGeminiRequestBody(cleanCurrent, visitorName);
        List<String> modelsToAttempt = getOrderedModelsToAttempt();

        Exception lastException = null;

        for (int i = 0; i < modelsToAttempt.size(); i++) {
            String targetModel = modelsToAttempt.get(i);
            long startTime = System.currentTimeMillis();

            Duration attemptTimeout = targetModel.equalsIgnoreCase(geminiModel)
                    ? Duration.ofMillis(4500)
                    : Duration.ofSeconds(12);

            String endpoint = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:streamGenerateContent?alt=sse&key=%s",
                    URLEncoder.encode(targetModel, StandardCharsets.UTF_8),
                    URLEncoder.encode(geminiApiKey, StandardCharsets.UTF_8)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", geminiApiKey)
                    .timeout(attemptTimeout)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            StringBuilder accumulated = new StringBuilder();
            int tokenCount = 0;
            long firstTokenTime = 0;

            try {
                HttpResponse<Stream<String>> response = getOrCreateHttpClient().send(request, HttpResponse.BodyHandlers.ofLines());
                int statusCode = response.statusCode();

                if (statusCode < 200 || statusCode >= 300) {
                    long latencyMs = System.currentTimeMillis() - startTime;
                    if (statusCode == 503 || statusCode == 404) {
                        markModelDegraded(targetModel);
                    }
                    log.warn("Gemini stream connection failed [model: '{}', status: {}, latency: {}ms]", targetModel, statusCode, latencyMs);
                    if (i < modelsToAttempt.size() - 1) {
                        continue;
                    }
                    throw new GeminiApiException(statusCode, "Gemini stream returned status " + statusCode);
                }

                try (Stream<String> lines = response.body()) {
                    for (String line : (Iterable<String>) lines::iterator) {
                        String trimmed = line.trim();
                        if (trimmed.startsWith("data: ")) {
                            String json = trimmed.substring(6).trim();
                            if (!json.isBlank()) {
                                try {
                                    JsonNode root = objectMapper.readTree(json);
                                    JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
                                    if (parts.isArray() && !parts.isEmpty()) {
                                        String text = parts.get(0).path("text").asText("");
                                        if (!text.isEmpty()) {
                                            if (firstTokenTime == 0) {
                                                firstTokenTime = System.currentTimeMillis() - startTime;
                                                log.info("Gemini stream first token received in {}ms [model: '{}']", firstTokenTime, targetModel);
                                            }
                                            tokenCount++;
                                            accumulated.append(text);
                                            tokenConsumer.accept(text);
                                        }
                                    }
                                } catch (Exception ex) {
                                    log.debug("Error parsing streaming chunk: {}", ex.getMessage());
                                }
                            }
                        }
                    }
                }

                if (tokenCount > 0) {
                    clearModelDegradation(targetModel);
                    long totalLatency = System.currentTimeMillis() - startTime;
                    log.info("Gemini streaming completed [model: '{}', tokens: {}, latency: {}ms]", targetModel, tokenCount, totalLatency);
                    return accumulated.toString().trim();
                }

                log.warn("Gemini stream returned 0 tokens for model '{}'", targetModel);
                if (i < modelsToAttempt.size() - 1) {
                    continue;
                }

            } catch (Exception ex) {
                long latencyMs = System.currentTimeMillis() - startTime;
                markModelDegraded(targetModel);
                log.warn("Gemini stream error on model '{}' after {}ms: {}", targetModel, latencyMs, ex.getMessage());
                lastException = ex;
                if (tokenCount == 0 && i < modelsToAttempt.size() - 1) {
                    continue;
                }
                if (tokenCount > 0) {
                    return accumulated.toString().trim();
                }
            }
        }

        if (lastException != null) {
            throw lastException;
        }
        throw new RuntimeException("All Gemini streaming attempts failed.");
    }

    private String callOpenAi(String currentMessage, String visitorName) throws Exception {
        String endpoint = "https://api.openai.com/v1/chat/completions";

        String systemInstructionWithVisitor = SYSTEM_PROMPT + "\nNote: The visitor's name is " +
                (visitorName != null && !visitorName.isBlank() ? visitorName : "Visitor") + ".";

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemInstructionWithVisitor),
                Map.of("role", "user", "content", currentMessage.trim())
        );

        Map<String, Object> requestPayload = Map.of(
                "model", openAiModel,
                "messages", messages,
                "max_tokens", 800,
                "temperature", 0.7
        );

        String jsonBody = objectMapper.writeValueAsString(requestPayload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + openAiApiKey)
                .timeout(Duration.ofSeconds(12))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = getOrCreateHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText().trim();
            }
            log.warn("OpenAI response missing message content: {}", response.body());
            return "Thank you for reaching out! How can we assist with your project today?";
        } else {
            log.error("OpenAI API error (HTTP {}): {}", response.statusCode(), response.body());
            throw new RuntimeException("OpenAI API returned status " + response.statusCode());
        }
    }

    private static String sanitizeGeminiModel(String model) {
        if (model == null || model.isBlank()) {
            return "gemini-3.8-flash";
        }
        String m = model.trim().replaceAll("[\"']", "");
        if (m.startsWith("models/")) {
            m = m.substring(7);
        }
        return m;
    }

    private static String sanitizeApiKey(String key) {
        if (key == null) return "";
        String trimmed = key.trim().replaceAll("[\"']", "");
        if (trimmed.equalsIgnoreCase("PASTE_YOUR_COPIED_KEY_HERE") ||
            trimmed.equalsIgnoreCase("your_gemini_api_key") ||
            trimmed.equalsIgnoreCase("re_your_api_key_here")) {
            return "";
        }
        return trimmed;
    }

    private static String tryReadFromDotEnv(String keyName) {
        List<Path> candidatePaths = List.of(
                Paths.get(".env"),
                Paths.get("designtouch-backend/.env"),
                Paths.get("../designtouch-backend/.env")
        );
        for (Path p : candidatePaths) {
            if (Files.isRegularFile(p)) {
                try {
                    List<String> lines = Files.readAllLines(p);
                    for (String line : lines) {
                        String trimmed = line.trim();
                        if (trimmed.startsWith(keyName + "=")) {
                            String val = trimmed.substring(keyName.length() + 1).trim();
                            val = sanitizeApiKey(val);
                            if (!val.isBlank()) {
                                return val;
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return "";
    }

    private static String determineErrorCategory(int statusCode, String errorMsg) {
        if (statusCode == 401 || statusCode == 403) {
            return "AUTHENTICATION_ERROR";
        } else if (statusCode == 429) {
            return "RATE_LIMIT_OR_QUOTA_EXCEEDED";
        } else if (statusCode == 503) {
            return "MODEL_CAPACITY_HIGH_DEMAND";
        } else if (statusCode == 404) {
            return "MODEL_NOT_FOUND";
        } else if (statusCode == 400) {
            return "INVALID_REQUEST";
        } else if (statusCode >= 500) {
            return "UPSTREAM_SERVER_ERROR";
        }
        return "UNKNOWN_ERROR";
    }

    private String extractGeminiErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "Empty response body from Gemini API";
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode errorNode = root.path("error");
            if (!errorNode.isMissingNode()) {
                String message = errorNode.path("message").asText("");
                String status = errorNode.path("status").asText("");
                int code = errorNode.path("code").asInt(0);
                String result = String.format("[%d %s] %s", code, status, message);
                if (geminiApiKey != null && !geminiApiKey.isBlank()) {
                    result = result.replace(geminiApiKey, "[REDACTED]");
                }
                return result;
            }
        } catch (Exception ignored) {
        }
        String sanitized = responseBody;
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            sanitized = sanitized.replace(geminiApiKey, "[REDACTED]");
        }
        return sanitized.length() > 300 ? sanitized.substring(0, 300) + "..." : sanitized;
    }

    @Getter
    public static class GeminiApiException extends RuntimeException {
        private final int statusCode;
        private final String category;

        public GeminiApiException(int statusCode, String message, String category) {
            super(message);
            this.statusCode = statusCode;
            this.category = (category != null && !category.isBlank()) ? category : "UNKNOWN";
        }

        public GeminiApiException(int statusCode, String message) {
            this(statusCode, message, determineErrorCategory(statusCode, message));
        }
    }
}
