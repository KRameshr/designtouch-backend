package com.designtouch.backend.service;

import com.designtouch.backend.dto.PortfolioPageContentRequest;
import com.designtouch.backend.dto.PortfolioPageContentResponse;
import com.designtouch.backend.dto.PortfolioPageTagRequest;
import com.designtouch.backend.dto.PortfolioPageTagResponse;
import com.designtouch.backend.entity.PortfolioPageContent;
import com.designtouch.backend.entity.PortfolioPageTag;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.PortfolioPageContentRepository;
import com.designtouch.backend.repository.PortfolioPageTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioPageContentService {

    private final PortfolioPageContentRepository contentRepository;
    private final PortfolioPageTagRepository tagRepository;
    private final MediaService mediaService;

    // ---------- Portfolio Intro Content ----------

    @Transactional(readOnly = true)
    public PortfolioPageContentResponse getPortfolioPageContent() {
        PortfolioPageContent content = contentRepository.findSingleton()
                .orElseGet(() -> PortfolioPageContent.builder()
                        .id((short) 1)
                        .eyebrow("OUR PORTFOLIO // SELECTED WORK")
                        .headlineLine1("Our Work")
                        .headlineLine2("Speaks")
                        .headlineHighlight("Louder.")
                        .description("Explore our curated portfolio of commercial campaigns, creator reels, and high-conversion visual narratives built to command attention.")
                        .build());
        return PortfolioPageContentResponse.from(content);
    }

    @Transactional
    public PortfolioPageContentResponse updatePortfolioPageContent(PortfolioPageContentRequest request) {
        PortfolioPageContent content = contentRepository.findSingleton()
                .orElseGet(() -> PortfolioPageContent.builder().id((short) 1).build());

        // Cloudinary video replacement / deletion lifecycle
        if (content.getIntroVideoPublicId() != null && !content.getIntroVideoPublicId().isBlank()) {
            boolean videoChanged = request.introVideoPublicId() != null &&
                    !request.introVideoPublicId().equals(content.getIntroVideoPublicId());
            boolean videoRemoved = (request.introVideoPublicId() == null || request.introVideoPublicId().isBlank()) &&
                    (request.introVideoUrl() == null || request.introVideoUrl().isBlank());

            if (videoChanged || videoRemoved) {
                log.info("Deleting replaced or removed Cloudinary intro video: {}", content.getIntroVideoPublicId());
                try {
                    mediaService.deleteVideo(content.getIntroVideoPublicId());
                } catch (Exception e) {
                    log.warn("Failed to delete old Cloudinary intro video: {}", e.getMessage());
                }
            }
        }

        content.setEyebrow(request.eyebrow());
        content.setHeadlineLine1(request.headlineLine1());
        content.setHeadlineLine2(request.headlineLine2());
        content.setHeadlineHighlight(request.headlineHighlight());
        content.setDescription(request.description());
        content.setIntroVideoUrl(request.introVideoUrl());
        content.setIntroVideoPublicId(request.introVideoPublicId());
        content.setUpdatedAt(OffsetDateTime.now());

        PortfolioPageContent saved = contentRepository.save(content);
        return PortfolioPageContentResponse.from(saved);
    }

    // ---------- Portfolio Category Tags ----------

    @Transactional(readOnly = true)
    public List<PortfolioPageTagResponse> getTags(boolean all) {
        List<PortfolioPageTag> tags = all
                ? tagRepository.findAllByOrderByDisplayOrderAsc()
                : tagRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc();

        return tags.stream()
                .map(PortfolioPageTagResponse::from)
                .toList();
    }

    @Transactional
    public PortfolioPageTagResponse createTag(PortfolioPageTagRequest request) {
        PortfolioPageTag tag = PortfolioPageTag.builder()
                .label(request.label())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();

        PortfolioPageTag saved = tagRepository.save(tag);
        return PortfolioPageTagResponse.from(saved);
    }

    @Transactional
    public PortfolioPageTagResponse updateTag(UUID id, PortfolioPageTagRequest request) {
        PortfolioPageTag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio tag not found: " + id));

        tag.setLabel(request.label());
        if (request.displayOrder() != null) tag.setDisplayOrder(request.displayOrder());
        if (request.isActive() != null) tag.setIsActive(request.isActive());
        tag.setUpdatedAt(OffsetDateTime.now());

        PortfolioPageTag saved = tagRepository.save(tag);
        return PortfolioPageTagResponse.from(saved);
    }

    @Transactional
    public void deleteTag(UUID id) {
        PortfolioPageTag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio tag not found: " + id));
        tagRepository.delete(tag);
    }
}

