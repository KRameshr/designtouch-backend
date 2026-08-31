package com.designtouch.backend.controller;

import com.designtouch.backend.dto.AboutContentResponse;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.AboutContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/about")
@RequiredArgsConstructor
public class AboutContentController {

    private final AboutContentRepository repository;

    @GetMapping
    public AboutContentResponse get() {
        return repository.findById((short) 1)
                .map(AboutContentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("About content not set up yet"));
    }
}
