package com.designtouch.backend.service;

import com.designtouch.backend.dto.*;
import com.designtouch.backend.entity.ChatLead;
import com.designtouch.backend.repository.ChatLeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatLeadRepository leadRepository;
    private final AiChatService aiChatService;
    private final ChatRateLimiter rateLimiter;

    @Transactional
    public ChatLeadResponse createOrGetLead(ChatLeadRequest request, String clientIp) {
        if (!rateLimiter.isAllowed("lead_" + clientIp)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Please wait a moment.");
        }

        String rawName = request.name() != null ? request.name().trim() : "";
        String rawEmail = request.email() != null ? request.email().trim().toLowerCase() : "";

        if (rawName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please provide your full name");
        }
        if (rawEmail.isBlank() || !rawEmail.contains("@") || !rawEmail.contains(".")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a valid email address");
        }

        Optional<ChatLead> existingOpt = leadRepository.findFirstByEmailIgnoreCaseOrderByCreatedAtDesc(rawEmail);
        ChatLead lead;

        if (existingOpt.isPresent()) {
            lead = existingOpt.get();
            // Update name if visitor provided an updated full name
            if (!rawName.equalsIgnoreCase(lead.getName())) {
                lead.setName(rawName);
            }
            lead.setUpdatedAt(OffsetDateTime.now());
            lead = leadRepository.save(lead);
            log.info("Reused existing chat lead with ID: {} for email: {}", lead.getId(), rawEmail);
        } else {
            lead = ChatLead.builder()
                    .name(rawName)
                    .email(rawEmail)
                    .conversationStatus("ACTIVE")
                    .source("WEBSITE_CHATBOT")
                    .build();
            lead = leadRepository.save(lead);
            log.info("Created new chat lead with ID: {} for email: {}", lead.getId(), rawEmail);
        }

        return new ChatLeadResponse(
                lead.getId(),
                lead.getName(),
                lead.getEmail(),
                lead.getConversationStatus(),
                lead.getCreatedAt()
        );
    }

    /**
     * Ephemeral conversation initialization. No database record is created or persisted.
     */
    public ChatConversationResponse createConversation(ChatConversationRequest request) {
        ChatLead lead = null;
        if (request != null && request.leadId() != null) {
            lead = leadRepository.findById(request.leadId()).orElse(null);
        }

        UUID ephemeralId = UUID.randomUUID();
        String visitorName = lead != null ? lead.getName() : "Visitor";
        UUID leadId = lead != null ? lead.getId() : (request != null ? request.leadId() : null);

        return new ChatConversationResponse(
                ephemeralId,
                leadId,
                visitorName,
                "ACTIVE",
                OffsetDateTime.now(),
                List.of()
        );
    }

    /**
     * Sends a chat message and returns fresh AI response.
     * Neither visitor message nor AI response is persisted to the database.
     */
    public ChatMessageResponse sendMessage(ChatMessageRequest request, String clientIp) {
        if (!rateLimiter.isAllowed("msg_" + clientIp)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "You are sending messages too quickly. Please wait a moment.");
        }

        String content = request != null && request.content() != null ? request.content().trim() : "";
        if (content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content cannot be empty.");
        }

        String leadName = resolveVisitorName(request);

        // Generate fresh AI response with no stored history
        String aiReplyContent = aiChatService.generateResponse(content, leadName);

        ChatMessageDto userDto = new ChatMessageDto(
                UUID.randomUUID(),
                "VISITOR",
                content,
                OffsetDateTime.now()
        );

        ChatMessageDto aiDto = new ChatMessageDto(
                UUID.randomUUID(),
                "ASSISTANT",
                aiReplyContent,
                OffsetDateTime.now()
        );

        return new ChatMessageResponse(userDto, aiDto, aiChatService.isConfigured());
    }

    /**
     * Streams AI tokens via SSE.
     * Starts with a fresh AI context and does not persist any messages to the database.
     */
    public void streamMessage(ChatMessageRequest request, String clientIp, SseEmitter emitter) {
        if (!rateLimiter.isAllowed("msg_" + clientIp)) {
            try {
                emitter.send(SseEmitter.event().data(Map.of("type", "error", "message", "You are sending messages too quickly. Please wait a moment.")));
                emitter.complete();
            } catch (Exception ignored) {}
            return;
        }

        String content = request != null && request.content() != null ? request.content().trim() : "";
        if (content.isBlank()) {
            try {
                emitter.send(SseEmitter.event().data(Map.of("type", "error", "message", "Message content cannot be empty.")));
                emitter.complete();
            } catch (Exception ignored) {}
            return;
        }

        String leadName = resolveVisitorName(request);

        ChatMessageDto userDto = new ChatMessageDto(
                UUID.randomUUID(),
                "VISITOR",
                content,
                OffsetDateTime.now()
        );

        // Emit "init" SSE event
        try {
            emitter.send(SseEmitter.event().data(Map.of(
                    "type", "init",
                    "userMessage", userDto,
                    "isAiConfigured", aiChatService.isConfigured()
            )));
        } catch (Exception e) {
            log.warn("Failed to send init SSE event: {}", e.getMessage());
            emitter.completeWithError(e);
            return;
        }

        // Stream AI tokens
        try {
            String fullReply = aiChatService.generateStreamingResponse(
                    content,
                    leadName,
                    token -> {
                        try {
                            emitter.send(SseEmitter.event().data(Map.of(
                                    "type", "token",
                                    "content", token
                            )));
                        } catch (Exception e) {
                            log.debug("Error emitting SSE token: {}", e.getMessage());
                        }
                    }
            );

            ChatMessageDto aiDto = new ChatMessageDto(
                    UUID.randomUUID(),
                    "ASSISTANT",
                    fullReply,
                    OffsetDateTime.now()
            );

            // Emit "done" event
            emitter.send(SseEmitter.event().data(Map.of(
                    "type", "done",
                    "aiMessage", aiDto,
                    "isAiConfigured", aiChatService.isConfigured()
            )));
            emitter.complete();

        } catch (Exception e) {
            log.error("Streaming chat failure: {}", e.getMessage());
            try {
                emitter.send(SseEmitter.event().data(Map.of(
                        "type", "error",
                        "message", "I apologize, but I encountered a connection issue while generating the response. Please try again."
                )));
                emitter.complete();
            } catch (Exception ignored) {}
        }
    }

    private String resolveVisitorName(ChatMessageRequest request) {
        if (request == null) return "Visitor";

        if (request.leadId() != null) {
            Optional<ChatLead> leadOpt = leadRepository.findById(request.leadId());
            if (leadOpt.isPresent()) {
                return leadOpt.get().getName();
            }
        }

        if (request.conversationId() != null) {
            Optional<ChatLead> leadOpt = leadRepository.findById(request.conversationId());
            if (leadOpt.isPresent()) {
                return leadOpt.get().getName();
            }
        }

        return "Visitor";
    }
}
