package com.designtouch.backend.controller;

import com.designtouch.backend.dto.AboutContentRequest;
import com.designtouch.backend.dto.AboutContentResponse;
import com.designtouch.backend.entity.AboutContent;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.AboutContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/about")
@RequiredArgsConstructor
public class AboutContentController {

    private final AboutContentRepository repository;

    @GetMapping
    public AboutContentResponse get() {
        return repository.findById((short) 1)
                .map(AboutContentResponse::from)
                .orElseGet(() -> {
                    AboutContent created = AboutContent.builder()
                            .id((short) 1)
                            .heading("WE ARE DESIGNTOUCH+")
                            .subheading("We are a team of passionate storytellers, editors and creators who turn ideas into powerful visual experiences.")
                            .ctaText("Let's Create Together")
                            .ctaLink("/contact")
                            .storyHeading("Turning Ideas Into Impact.")
                            .storyParagraph1("DesignTouch+ was born out of a love for storytelling and a passion for visual creativity.")
                            .storyParagraph2("We believe every brand has a story worth telling, and our job is to tell it the right way.")
                            .founderName("Rohit Sharma")
                            .founderDesignation("Founder, DesignTouch+")
                            .build();
                    return AboutContentResponse.from(repository.save(created));
                });
    }

    @PutMapping
    public AboutContentResponse update(@RequestBody AboutContentRequest request) {
        AboutContent entity = repository.findById((short) 1)
                .orElseGet(() -> AboutContent.builder().id((short) 1).build());

        if (request.heading() != null) entity.setHeading(request.heading());
        if (request.subheading() != null) entity.setSubheading(request.subheading());
        if (request.ctaText() != null) entity.setCtaText(request.ctaText());
        if (request.ctaLink() != null) entity.setCtaLink(request.ctaLink());
        if (request.heroBannerImageUrl() != null) entity.setHeroBannerImageUrl(request.heroBannerImageUrl());
        if (request.heroBannerImagePublicId() != null) entity.setHeroBannerImagePublicId(request.heroBannerImagePublicId());
        if (request.storyHeading() != null) entity.setStoryHeading(request.storyHeading());
        if (request.storyParagraph1() != null) entity.setStoryParagraph1(request.storyParagraph1());
        if (request.storyParagraph2() != null) entity.setStoryParagraph2(request.storyParagraph2());
        if (request.storyImageUrl() != null) {
            entity.setStoryImageUrl(request.storyImageUrl());
            entity.setStoryVideoThumbnailUrl(request.storyImageUrl());
        }
        if (request.storyVideoThumbnailUrl() != null) {
            entity.setStoryVideoThumbnailUrl(request.storyVideoThumbnailUrl());
            if (entity.getStoryImageUrl() == null) {
                entity.setStoryImageUrl(request.storyVideoThumbnailUrl());
            }
        }
        if (request.founderName() != null) entity.setFounderName(request.founderName());
        if (request.founderDesignation() != null) entity.setFounderDesignation(request.founderDesignation());
        if (request.founderSignatureUrl() != null) entity.setFounderSignatureUrl(request.founderSignatureUrl());
        if (request.storyVideoUrl() != null) entity.setStoryVideoUrl(request.storyVideoUrl());

        return AboutContentResponse.from(repository.save(entity));
    }

    @DeleteMapping("/hero-banner")
    public AboutContentResponse deleteHeroBanner() {
        AboutContent entity = repository.findById((short) 1)
                .orElseThrow(() -> new ResourceNotFoundException("About content not found"));
        entity.setHeroBannerImageUrl(null);
        entity.setHeroBannerImagePublicId(null);
        return AboutContentResponse.from(repository.save(entity));
    }
}
