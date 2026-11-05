package com.designtouch.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AiChatServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

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
