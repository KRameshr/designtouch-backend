package com.designtouch.backend.controller;

import com.designtouch.backend.dto.ContactMessageRequest;
import com.designtouch.backend.entity.ContactMessage;
import com.designtouch.backend.repository.ContactMessageRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactMessageController {

    private final ContactMessageRepository repository;

    // "SEND MESSAGE" form submit — public, no auth required
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void submit(@Valid @RequestBody ContactMessageRequest request) {
        ContactMessage message = ContactMessage.builder()
                .name(request.name())
                .email(request.email())
                .company(request.company())
                .subject(request.subject())
                .message(request.message())
                .build();
        repository.save(message);
    }
}
