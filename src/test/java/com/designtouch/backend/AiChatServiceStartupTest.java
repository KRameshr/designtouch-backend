package com.designtouch.backend;

import com.designtouch.backend.service.AiChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AiChatServiceStartupTest {

    @Configuration
    @Import(AiChatService.class)
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Test
    @DisplayName("Verify Spring Boot context loads with 'dev' profile and resolves AiChatService without circular placeholders")
    void contextLoadsWithDevProfile_noCircularPlaceholderError() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(TestConfig.class)
                .profiles("dev")
                .web(WebApplicationType.NONE)
                .run()) {

            AiChatService aiChatService = context.getBean(AiChatService.class);
            assertNotNull(aiChatService);

            // Model must be configured to gemini-3.8-flash as defined in application-dev.properties
            assertEquals("gemini-3.8-flash", aiChatService.getGeminiModel());
            assertEquals("gemini", aiChatService.getProvider());
        }
    }

    @Test
    @DisplayName("Verify Spring Boot context loads with 'dev' profile and unconfigured key returns safe fallback")
    void contextLoadsWithDevProfile_unconfiguredFallback() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(TestConfig.class)
                .profiles("dev")
                .web(WebApplicationType.NONE)
                .properties("app.ai.gemini.api-key=", "GEMINI_API_KEY=")
                .run()) {

            AiChatService aiChatService = context.getBean(AiChatService.class);
            assertNotNull(aiChatService);
            assertEquals("gemini-3.8-flash", aiChatService.getGeminiModel());
            assertEquals("gemini", aiChatService.getProvider());
        }
    }

    @Test
    @DisplayName("Verify AiChatService startup with GEMINI_API_KEY system property")
    void contextLoadsWithExplicitApiKey_isConfiguredTrue() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(TestConfig.class)
                .profiles("dev")
                .web(WebApplicationType.NONE)
                .properties("app.ai.gemini.api-key=AIzaSyTestKey123")
                .run()) {

            AiChatService aiChatService = context.getBean(AiChatService.class);
            assertNotNull(aiChatService);
            assertTrue(aiChatService.isConfigured());
            assertEquals("gemini-3.8-flash", aiChatService.getGeminiModel());
        }
    }
}
