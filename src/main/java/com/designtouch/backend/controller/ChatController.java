package com.designtouch.backend.controller;

import com.designtouch.backend.dto.*;
import com.designtouch.backend.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/leads")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatLeadResponse createOrGetLead(
            @Valid @RequestBody ChatLeadRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = extractClientIp(httpRequest);
        return chatService.createOrGetLead(request, clientIp);
    }

    @PostMapping("/conversations")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatConversationResponse createConversation(
            @Valid @RequestBody ChatConversationRequest request
    ) {
        return chatService.createConversation(request);
    }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.OK)
    public ChatMessageResponse sendMessage(
            @Valid @RequestBody ChatMessageRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = extractClientIp(httpRequest);
        return chatService.sendMessage(request, clientIp);
    }

    @PostMapping(value = "/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStream(
            @Valid @RequestBody ChatMessageRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = extractClientIp(httpRequest);
        SseEmitter emitter = new SseEmitter(60_000L);
        CompletableFuture.runAsync(() -> chatService.streamMessage(request, clientIp, emitter));
        return emitter;
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }
}

