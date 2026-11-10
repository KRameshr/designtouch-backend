package com.designtouch.backend.controller;

import com.designtouch.backend.dto.*;
import com.designtouch.backend.exception.GlobalExceptionHandler;
import com.designtouch.backend.service.AiChatService;
import com.designtouch.backend.service.ChatRateLimiter;
import com.designtouch.backend.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ChatService chatService;
    private AtomicBoolean rateLimitTriggered;
    private AtomicBoolean leadNotFound;
    private AtomicBoolean aiConfigured;

    private final UUID sampleLeadId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID sampleConvId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setUp() {
        rateLimitTriggered = new AtomicBoolean(false);
        leadNotFound = new AtomicBoolean(false);
        aiConfigured = new AtomicBoolean(false);

        // Standalone subclass of ChatService for fast in-memory controller test execution
        chatService = new ChatService(null, null, null) {
            @Override
            public ChatLeadResponse createOrGetLead(ChatLeadRequest request, String clientIp) {
                if (rateLimitTriggered.get()) {
                    throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Please wait a moment.");
                }
                if (request.name() == null || request.name().isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please provide your full name");
                }
                if (request.email() == null || request.email().isBlank() || !request.email().contains("@")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a valid email address");
                }

                return new ChatLeadResponse(
                        sampleLeadId,
                        request.name().trim(),
                        request.email().trim().toLowerCase(),
                        "ACTIVE",
                        OffsetDateTime.now()
                );
            }

            @Override
            public ChatConversationResponse createConversation(ChatConversationRequest request) {
                if (leadNotFound.get()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found. Please provide your details first.");
                }
                return new ChatConversationResponse(
                        sampleConvId,
                        request.leadId(),
                        "Test Visitor",
                        "ACTIVE",
                        OffsetDateTime.now(),
                        List.of()
                );
            }

            @Override
            public ChatMessageResponse sendMessage(ChatMessageRequest request, String clientIp) {
                if (rateLimitTriggered.get()) {
                    throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "You are sending messages too quickly. Please wait a moment.");
                }
                if (request.content() == null || request.content().isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content cannot be empty.");
                }

                ChatMessageDto userMsg = new ChatMessageDto(
                        UUID.randomUUID(),
                        "VISITOR",
                        request.content().trim(),
                        OffsetDateTime.now()
                );

                String aiContent = aiConfigured.get()
                        ? "We offer professional video editing, motion graphics, and YouTube thumbnail design!"
                        : AiChatService.UNCONFIGURED_FALLBACK;

                ChatMessageDto aiMsg = new ChatMessageDto(
                        UUID.randomUUID(),
                        "ASSISTANT",
                        aiContent,
                        OffsetDateTime.now()
                );

                return new ChatMessageResponse(userMsg, aiMsg, aiConfigured.get());
            }

        };

        ChatController controller = new ChatController(chatService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("1. POST /api/chat/leads saves visitor lead with 201 Created and returns lead ID")
    void createLead_validData_returns201Created() throws Exception {
        ChatLeadRequest request = new ChatLeadRequest("Sarah Connor", "sarah@skynet.com");

        mockMvc.perform(post("/api/chat/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(sampleLeadId.toString()))
                .andExpect(jsonPath("$.name").value("Sarah Connor"))
                .andExpect(jsonPath("$.email").value("sarah@skynet.com"))
                .andExpect(jsonPath("$.conversationStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("2. POST /api/chat/leads rejects missing name or invalid email with 400 Bad Request")
    void createLead_invalidData_returns400() throws Exception {
        ChatLeadRequest invalidEmail = new ChatLeadRequest("John Doe", "not-an-email");

        mockMvc.perform(post("/api/chat/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmail)))
                .andExpect(status().isBadRequest());

        ChatLeadRequest blankName = new ChatLeadRequest("", "john@example.com");

        mockMvc.perform(post("/api/chat/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankName)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("3. POST /api/chat/conversations creates conversation session linked to lead ID")
    void createConversation_validLead_returns201Created() throws Exception {
        ChatConversationRequest request = new ChatConversationRequest(sampleLeadId, "Website Chat");

        mockMvc.perform(post("/api/chat/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(sampleConvId.toString()))
                .andExpect(jsonPath("$.leadId").value(sampleLeadId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("4. POST /api/chat/messages sends visitor message and returns persisted user message + AI response")
    void sendMessage_validInput_returnsMessages() throws Exception {
        aiConfigured.set(true);
        ChatMessageRequest request = new ChatMessageRequest(sampleConvId, "Tell me about YouTube thumbnails");

        mockMvc.perform(post("/api/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userMessage.senderType").value("VISITOR"))
                .andExpect(jsonPath("$.userMessage.content").value("Tell me about YouTube thumbnails"))
                .andExpect(jsonPath("$.aiMessage.senderType").value("ASSISTANT"))
                .andExpect(jsonPath("$.aiMessage.content", containsString("thumbnail")))
                .andExpect(jsonPath("$.isAiConfigured").value(true));
    }

    @Test
    @DisplayName("5. POST /api/chat/messages handles unconfigured AI safely with friendly fallback notice")
    void sendMessage_unconfiguredAi_returnsFallbackMessage() throws Exception {
        aiConfigured.set(false);
        ChatMessageRequest request = new ChatMessageRequest(sampleConvId, "What are your prices?");

        mockMvc.perform(post("/api/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userMessage.content").value("What are your prices?"))
                .andExpect(jsonPath("$.aiMessage.content", containsString("configuration mode")))
                .andExpect(jsonPath("$.isAiConfigured").value(false));
    }

    @Test
    @DisplayName("6. POST /api/chat/messages enforces rate limiting when threshold exceeded")
    void sendMessage_rateLimited_returns429() throws Exception {
        rateLimitTriggered.set(true);
        ChatMessageRequest request = new ChatMessageRequest(sampleConvId, "Hello spam");

        mockMvc.perform(post("/api/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests());
    }
}

