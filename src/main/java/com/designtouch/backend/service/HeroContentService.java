package com.designtouch.backend.service;

import com.designtouch.backend.dto.HeroContentRequest;
import com.designtouch.backend.dto.HeroContentResponse;
import com.designtouch.backend.entity.HeroContent;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.HeroContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class HeroContentService {

    private final HeroContentRepository repository;

    @Transactional(readOnly = true)
    public HeroContentResponse getHeroContent() {
        return repository.findSingleton()
                .map(HeroContentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Hero content not configured yet"));
    }

    @Transactional
    public HeroContentResponse updateHeroContent(HeroContentRequest request) {
        HeroContent hero = repository.findSingleton()
                .orElseGet(() -> HeroContent.builder().id((short) 1).build());

        hero.setEyebrow(request.eyebrow());
        hero.setHeadlineLine1(request.headlineLine1());
        hero.setHeadlineLine2(request.headlineLine2());
        hero.setHeadlineLine3(request.headlineLine3());
        hero.setHeadlineHighlight(request.headlineHighlight());
        hero.setSubheading(request.subheading());
        hero.setPrimaryCtaText(request.primaryCtaText());
        hero.setPrimaryCtaLink(request.primaryCtaLink());
        hero.setSecondaryCtaText(request.secondaryCtaText());
        hero.setSecondaryCtaLink(request.secondaryCtaLink());
        hero.setVideoUrl(request.videoUrl());
        hero.setBadgeText(request.badgeText());
        hero.setSpecBadgeText(request.specBadgeText());
        hero.setUpdatedAt(OffsetDateTime.now());

        HeroContent saved = repository.save(hero);
        return HeroContentResponse.from(saved);
    }
}

