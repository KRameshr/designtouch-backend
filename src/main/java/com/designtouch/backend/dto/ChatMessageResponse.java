package com.designtouch.backend.dto;

public record ChatMessageResponse(
        ChatMessageDto userMessage,
        ChatMessageDto aiMessage,
        boolean isAiConfigured
) {}

