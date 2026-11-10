package com.designtouch.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class AiChatServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TEST_API_KEY = "test-only-gemini-key";

    @Test
    @DisplayName("1. AiChatService detects unconfigured API key correctly")
    void isConfigured_emptyKey_returnsFalse() {
        AiChatService service = new AiChatService("gemini", "", "gemini-3.8-flash", "", "gpt-4o-mini", objectMapper, false);
        assertFalse(service.isConfigured());

        AiChatService openAiService = new AiChatService("openai", "", "gemini-3.8-flash", "", "gpt-4o-mini", objectMapper, false);
        assertFalse(openAiService.isConfigured());
    }

    @Test
    @DisplayName("2. AiChatService treats placeholder string 'PASTE_YOUR_COPIED_KEY_HERE' as unconfigured")
    void isConfigured_placeholderKey_returnsFalse() {
        AiChatService service = new AiChatService("gemini", "PASTE_YOUR_COPIED_KEY_HERE", "gemini-3.8-flash", "", "gpt-4o-mini", objectMapper, false);
        assertFalse(service.isConfigured());

        AiChatService serviceQuotes = new AiChatService("gemini", "\"PASTE_YOUR_COPIED_KEY_HERE\"", "gemini-3.8-flash", "", "gpt-4o-mini", objectMapper, false);
        assertFalse(serviceQuotes.isConfigured());
    }

    @Test
    @DisplayName("3. AiChatService detects valid configured API key")
    void isConfigured_validKey_returnsTrue() {
        AiChatService geminiService = new AiChatService("gemini", "AIzaSyFakeKey123", "gemini-3.8-flash", "", "gpt-4o-mini", objectMapper, false);
        assertTrue(geminiService.isConfigured());

        AiChatService openAiService = new AiChatService("openai", "", "gemini-3.8-flash", "sk-fakeKey123", "gpt-4o-mini", objectMapper, false);
        assertTrue(openAiService.isConfigured());
    }

    @Test
    @DisplayName("4. Unconfigured AI returns safe fallback without network calls")
    void generateResponse_unconfigured_returnsSafeFallback() {
        AiChatService service = new AiChatService("gemini", "", "gemini-3.8-flash", "", "gpt-4o-mini", objectMapper, false);
        String response = service.generateResponse("Hello, can you help me?", "Test Visitor");

        assertNotNull(response);
        assertTrue(response.contains("configuration mode"));
        assertTrue(response.contains("GEMINI_API_KEY"));
        assertTrue(response.contains("designtouchplus@gmail.com"));
    }

    @Test
    @DisplayName("5. Accurately preserves configured model 'gemini-3.8-flash' and strips quotes/prefixes")
    void preservesConfiguredModel() {
        AiChatService service = new AiChatService("gemini", "", "gemini-3.8-flash", "", "gpt-4o-mini", objectMapper, false);
        assertEquals("gemini-3.8-flash", service.getGeminiModel());

        AiChatService serviceWithQuotes = new AiChatService("gemini", "", "\"gemini-3.8-flash\"", "", "gpt-4o-mini", objectMapper, false);
        assertEquals("gemini-3.8-flash", serviceWithQuotes.getGeminiModel());

        AiChatService serviceWithPrefix = new AiChatService("gemini", "", "models/gemini-3.8-flash", "", "gpt-4o-mini", objectMapper, false);
        assertEquals("gemini-3.8-flash", serviceWithPrefix.getGeminiModel());
    }

    @Test
    @DisplayName("Gemini generateContent request omits deprecated sampling parameters")
    void generateResponse_sendsCompatibleGeminiRequest() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        AtomicReference<HttpRequest> sentRequest = new AtomicReference<>();
        stubSend(httpClient, response, sentRequest);
        org.mockito.Mockito.when(response.statusCode()).thenReturn(200);
        org.mockito.Mockito.when(response.body()).thenReturn(
                "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Hello from Gemini\"}]}}]}"
        );

        AiChatService service = configuredService(httpClient);
        assertEquals("Hello from Gemini", service.generateResponse("Hello", "Test Visitor"));

        HttpRequest request = sentRequest.get();
        assertNotNull(request);
        assertThat(request.uri().getPath()).endsWith(":generateContent");
        JsonNode body = objectMapper.readTree(readRequestBody(request));
        assertThat(body.path("generationConfig").path("temperature").isMissingNode()).isTrue();
        assertThat(body.path("generationConfig").path("topP").isMissingNode()).isTrue();
        assertThat(body.path("generationConfig").path("topK").isMissingNode()).isTrue();
        assertEquals(800, body.path("generationConfig").path("maxOutputTokens").asInt());
    }

    @Test
    @DisplayName("Gemini streamGenerateContent request omits deprecated sampling parameters")
    void generateStreamingResponse_sendsCompatibleGeminiRequest() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<Stream<String>> response = mock(HttpResponse.class);
        AtomicReference<HttpRequest> sentRequest = new AtomicReference<>();
        String chunk = "data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Streamed reply\"}]}}]}";
        stubSend(httpClient, response, sentRequest);
        org.mockito.Mockito.when(response.statusCode()).thenReturn(200);
        org.mockito.Mockito.when(response.body()).thenReturn(Stream.of(chunk));

        AiChatService service = configuredService(httpClient);
        AtomicReference<String> streamedToken = new AtomicReference<>();
        assertEquals("Streamed reply", service.generateStreamingResponse("Hello", "Test Visitor", streamedToken::set));
        assertEquals("Streamed reply", streamedToken.get());

        HttpRequest request = sentRequest.get();
        assertNotNull(request);
        assertThat(request.uri().getPath()).endsWith(":streamGenerateContent");
        assertThat(request.uri().getQuery()).contains("alt=sse");
        JsonNode body = objectMapper.readTree(readRequestBody(request));
        assertThat(body.path("generationConfig").path("temperature").isMissingNode()).isTrue();
        assertThat(body.path("generationConfig").path("topP").isMissingNode()).isTrue();
        assertThat(body.path("generationConfig").path("topK").isMissingNode()).isTrue();
        assertEquals(800, body.path("generationConfig").path("maxOutputTokens").asInt());
    }

    private AiChatService configuredService(HttpClient httpClient) {
        AiChatService service = new AiChatService(
                "gemini", TEST_API_KEY, "gemini-3.8-flash", "", "gpt-4o-mini", objectMapper, false
        );
        ReflectionTestUtils.setField(service, "httpClient", httpClient);
        return service;
    }

    private static <T> void stubSend(HttpClient httpClient, HttpResponse<T> response,
                                     AtomicReference<HttpRequest> sentRequest) throws Exception {
        doAnswer(invocation -> {
            sentRequest.set(invocation.getArgument(0));
            return response;
        }).when(httpClient).send(any(HttpRequest.class), any());
    }

    private static String readRequestBody(HttpRequest request) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CountDownLatch completed = new CountDownLatch(1);
        request.bodyPublisher().orElseThrow().subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ByteBuffer item) {
                byte[] chunk = new byte[item.remaining()];
                item.get(chunk);
                bytes.write(chunk, 0, chunk.length);
            }

            @Override
            public void onError(Throwable throwable) {
                completed.countDown();
            }

            @Override
            public void onComplete() {
                completed.countDown();
            }
        });
        assertTrue(completed.await(1, TimeUnit.SECONDS), "request body should be emitted promptly");
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("6. System prompt embeds all required DesignTouch+ services and business guidelines")
    void systemPrompt_containsAllRequiredServices() {
        String prompt = AiChatService.SYSTEM_PROMPT;

        // Agency name and founder
        assertTrue(prompt.contains("DesignTouch+"));
        assertTrue(prompt.contains("Rohit Sharma"));

        // All 7+ core services
        assertTrue(prompt.contains("Video Editing"));
        assertTrue(prompt.contains("Motion Graphics"));
        assertTrue(prompt.contains("YouTube Thumbnail Design") || prompt.contains("Thumbnail Design"));
        assertTrue(prompt.contains("Brand Films") || prompt.contains("Commercial Ads"));
        assertTrue(prompt.contains("Content Creation") || prompt.contains("Social Media Content"));
        assertTrue(prompt.contains("YouTube Management"));
        assertTrue(prompt.contains("Graphic Design"));
        assertTrue(prompt.contains("Branding"));

        // Price safeguard guideline
        assertTrue(prompt.contains("Never invent or promise specific prices"));
    }

    @Test
    @DisplayName("7. GeminiApiException preserves status codes and error messages")
    void geminiApiException_preservesStatusCodeAndMessage() {
        AiChatService.GeminiApiException ex429 = new AiChatService.GeminiApiException(429, "Rate limit exceeded");
        assertEquals(429, ex429.getStatusCode());
        assertEquals("Rate limit exceeded", ex429.getMessage());

        AiChatService.GeminiApiException ex401 = new AiChatService.GeminiApiException(401, "API key invalid");
        assertEquals(401, ex401.getStatusCode());
        assertEquals("API key invalid", ex401.getMessage());
    }
}
