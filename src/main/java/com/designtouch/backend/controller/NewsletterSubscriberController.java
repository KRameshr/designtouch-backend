package com.designtouch.backend.controller;

import com.designtouch.backend.dto.NewsletterSubscribeRequest;
import com.designtouch.backend.entity.NewsletterSubscriber;
import com.designtouch.backend.repository.NewsletterSubscriberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterSubscriberController {

    private final NewsletterSubscriberRepository repository;

    // "STAY AHEAD OF THE CURVE" box (Blog page) + Footer newsletter box
    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@Valid @RequestBody NewsletterSubscribeRequest request) {
        if (repository.existsByEmail(request.email())) {
            // Already subscribed — treat as success rather than erroring,
            // so the frontend doesn't need to special-case this.
            return ResponseEntity.ok().build();
        }
        NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
                .email(request.email())
                .build();
        repository.save(subscriber);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
