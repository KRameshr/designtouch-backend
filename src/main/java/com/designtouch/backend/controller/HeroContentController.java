package com.designtouch.backend.controller;

import com.designtouch.backend.dto.HeroContentRequest;
import com.designtouch.backend.dto.HeroContentResponse;
import com.designtouch.backend.service.HeroContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hero")
@RequiredArgsConstructor
public class HeroContentController {

    private final HeroContentService heroContentService;

    @GetMapping
    public HeroContentResponse get() {
        return heroContentService.getHeroContent();
    }

    @PutMapping
    public HeroContentResponse update(@Valid @RequestBody HeroContentRequest request) {
        return heroContentService.updateHeroContent(request);
    }
}

