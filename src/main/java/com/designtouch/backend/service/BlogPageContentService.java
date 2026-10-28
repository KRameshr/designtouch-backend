package com.designtouch.backend.service;

import com.designtouch.backend.dto.BlogPageContentRequest;
import com.designtouch.backend.dto.BlogPageContentResponse;
import com.designtouch.backend.entity.BlogPageContent;
import com.designtouch.backend.repository.BlogPageContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogPageContentService {

    private final BlogPageContentRepository repository;
    private final MediaService mediaService;

    @Transactional(readOnly = true)
    public BlogPageContentResponse getBlogPageContent() {
        BlogPageContent content = repository.findById((short) 1).orElseGet(this::createDefaultSingleton);
        return BlogPageContentResponse.from(content);
    }

    @Transactional
    public BlogPageContentResponse updateBlogPageContent(BlogPageContentRequest request) {
        BlogPageContent content = repository.findById((short) 1).orElseGet(this::createDefaultSingleton);

        if (request.heroEyebrow() != null) content.setHeroEyebrow(request.heroEyebrow().trim());
        if (request.heroHeadlineLine1() != null) content.setHeroHeadlineLine1(request.heroHeadlineLine1().trim());
        if (request.heroHeadlineLine2() != null) content.setHeroHeadlineLine2(request.heroHeadlineLine2().trim());
        if (request.heroHeadlineHighlight() != null) content.setHeroHeadlineHighlight(request.heroHeadlineHighlight().trim());
        if (request.heroDescription() != null) content.setHeroDescription(request.heroDescription().trim());
        if (request.heroCtaText() != null) content.setHeroCtaText(request.heroCtaText().trim());
        if (request.heroCtaLink() != null) content.setHeroCtaLink(request.heroCtaLink().trim());
        if (request.heroBannerImageUrl() != null) content.setHeroBannerImageUrl(request.heroBannerImageUrl().trim());
        if (request.heroBannerImagePublicId() != null) content.setHeroBannerImagePublicId(request.heroBannerImagePublicId().trim());
        if (request.heroBannerImageAlt() != null) content.setHeroBannerImageAlt(request.heroBannerImageAlt().trim());
        if (request.heroBannerObjectPosition() != null) content.setHeroBannerObjectPosition(request.heroBannerObjectPosition().trim());
        content.setUpdatedAt(OffsetDateTime.now());

        BlogPageContent saved = repository.save(content);
        return BlogPageContentResponse.from(saved);
    }

    @Transactional
    public BlogPageContentResponse deleteHeroBannerImage() {
        BlogPageContent content = repository.findById((short) 1).orElseGet(this::createDefaultSingleton);

        if (content.getHeroBannerImagePublicId() != null && !content.getHeroBannerImagePublicId().isBlank()) {
            try {
                mediaService.deleteImage(content.getHeroBannerImagePublicId());
            } catch (Exception e) {
                log.warn("Could not delete old blog hero banner image {}: {}",
                        content.getHeroBannerImagePublicId(), e.getMessage());
            }
        }

        content.setHeroBannerImageUrl(null);
        content.setHeroBannerImagePublicId(null);
        content.setUpdatedAt(OffsetDateTime.now());
        BlogPageContent saved = repository.save(content);
        return BlogPageContentResponse.from(saved);
    }

    private BlogPageContent createDefaultSingleton() {
        return repository.save(BlogPageContent.builder()
                .id((short) 1)
                .heroEyebrow("OUR BLOG")
                .heroHeadlineLine1("INSIGHTS THAT")
                .heroHeadlineLine2("INSPIRE")
                .heroHeadlineHighlight("CREATIVITY.")
                .heroDescription("Tips, trends and tutorials on video editing, motion graphics, content creation and growing your brand.")
                .heroCtaText("EXPLORE ALL ARTICLES")
                .heroCtaLink("#articles")
                .heroBannerImageUrl("https://res.cloudinary.com/dggikfrj5/image/upload/v1792669852/designtouch/services/cygu80p1hmhkqgvpc3au.png")
                .heroBannerImagePublicId("designtouch/services/cygu80p1hmhkqgvpc3au")
                .heroBannerImageAlt("DesignTouch+ Creative Workspace Studio")
                .heroBannerObjectPosition("center right")
                .updatedAt(OffsetDateTime.now())
                .build());
    }
}
