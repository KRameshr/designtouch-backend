package com.designtouch.backend.controller;

import com.designtouch.backend.dto.ContactPageContentRequest;
import com.designtouch.backend.dto.ContactPageContentResponse;
import com.designtouch.backend.service.ContactPageContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact-page")
@RequiredArgsConstructor
public class ContactPageContentController {

    private final ContactPageContentService service;

    @GetMapping
    public ContactPageContentResponse getContent() {
        return service.getContactPageContent();
    }

    @PutMapping
    public ContactPageContentResponse updateContent(@Valid @RequestBody ContactPageContentRequest request) {
        return service.updateContactPageContent(request);
    }

    @DeleteMapping("/hero-banner")
    public ContactPageContentResponse deleteHeroBanner() {
        return service.deleteHeroBannerImage();
    }
}

