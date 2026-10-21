package com.designtouch.backend;

import com.designtouch.backend.dto.PortfolioItemRequest;
import com.designtouch.backend.entity.AdminUser;
import com.designtouch.backend.entity.enums.ContentType;
import com.designtouch.backend.entity.enums.PortfolioCategory;
import com.designtouch.backend.repository.AdminUserRepository;
import com.designtouch.backend.security.JwtService;
import com.designtouch.backend.service.MediaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class PortfolioItemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.designtouch.backend.repository.PortfolioItemRepository itemRepository;

    @Autowired
    private com.designtouch.backend.repository.PortfolioCategoryRepository categoryRepository;

    @MockBean
    private MediaService mediaService;

    private String adminToken;

    @BeforeEach
    void setUp() {
        AdminUser admin = adminUserRepository.findAll().stream().findFirst().orElseThrow();
        adminToken = jwtService.generateToken(admin.getUsername());
    }

    @Test
    void testGetPortfolio_public_returnsList() throws Exception {
        mockMvc.perform(get("/api/portfolio?contentType=THUMBNAIL_DESIGN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/portfolio?contentType=VIDEO_EDITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testCreatePortfolio_unauthenticated_forbidden() throws Exception {
        PortfolioItemRequest request = new PortfolioItemRequest(
                "Brand Film 2026",
                ContentType.VIDEO_EDITING,
                PortfolioCategory.COMMERCIAL,
                "Commercial",
                "High budget ad",
                "01:30",
                "https://res.cloudinary.com/demo/video/upload/sample.mp4",
                null,
                null,
                "designtouch/portfolio/sample",
                1,
                true
        );

        mockMvc.perform(post("/api/portfolio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testPortfolioLifecycle_admin_createUpdateWithVideoReplace_andDelete() throws Exception {
        // 1. Create project with video
        PortfolioItemRequest createReq = new PortfolioItemRequest(
                "Automotive Showcase",
                ContentType.VIDEO_EDITING,
                PortfolioCategory.COMMERCIAL,
                "Automotive",
                "Cinematic color grade",
                "02:15",
                "https://res.cloudinary.com/demo/video/upload/v1/designtouch/portfolio/auto_v1.mp4",
                null,
                null,
                "designtouch/portfolio/auto_v1",
                1,
                true
        );

        String createRes = mockMvc.perform(post("/api/portfolio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Automotive Showcase"))
                .andExpect(jsonPath("$.videoPublicId").value("designtouch/portfolio/auto_v1"))
                .andReturn().getResponse().getContentAsString();

        String projectId = objectMapper.readTree(createRes).get("id").asText();

        // 2. Update project and replace video
        PortfolioItemRequest updateReq = new PortfolioItemRequest(
                "Automotive Showcase 4K",
                ContentType.VIDEO_EDITING,
                PortfolioCategory.COMMERCIAL,
                "Automotive",
                "Updated cut",
                "02:20",
                "https://res.cloudinary.com/demo/video/upload/v1/designtouch/portfolio/auto_v2.mp4",
                null,
                null,
                "designtouch/portfolio/auto_v2",
                1,
                true
        );

        mockMvc.perform(put("/api/portfolio/" + projectId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.videoPublicId").value("designtouch/portfolio/auto_v2"));

        // Verify old video was deleted
        verify(mediaService).deleteVideo("designtouch/portfolio/auto_v1");

        // 3. Delete project
        mockMvc.perform(delete("/api/portfolio/" + projectId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify new video was deleted on project deletion
        verify(mediaService).deleteVideo("designtouch/portfolio/auto_v2");
    }

    @Test
    void testThumbnailLifecycle_admin_createUpdateWithImageReplace_andDelete() throws Exception {
        // 1. Create thumbnail design item
        PortfolioItemRequest createReq = new PortfolioItemRequest(
                "High CTR Finance Thumbnail",
                ContentType.THUMBNAIL_DESIGN,
                null,
                "Finance",
                "High CTR graphic",
                null,
                null,
                "https://res.cloudinary.com/demo/image/upload/v1/designtouch/thumbnails/thumb_v1.webp",
                "https://res.cloudinary.com/demo/image/upload/v1/designtouch/thumbnails/thumb_v1.webp",
                null,
                1,
                false,
                null,
                "designtouch/thumbnails/thumb_v1"
        );

        String createRes = mockMvc.perform(post("/api/portfolio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("High CTR Finance Thumbnail"))
                .andExpect(jsonPath("$.imagePublicId").value("designtouch/thumbnails/thumb_v1"))
                .andExpect(jsonPath("$.videoUrl").isEmpty())
                .andReturn().getResponse().getContentAsString();

        String projectId = objectMapper.readTree(createRes).get("id").asText();

        // 2. Update thumbnail and replace image
        PortfolioItemRequest updateReq = new PortfolioItemRequest(
                "High CTR Finance Thumbnail - Revised",
                ContentType.THUMBNAIL_DESIGN,
                null,
                "Finance",
                "High CTR graphic revised",
                null,
                null,
                "https://res.cloudinary.com/demo/image/upload/v2/designtouch/thumbnails/thumb_v2.webp",
                "https://res.cloudinary.com/demo/image/upload/v2/designtouch/thumbnails/thumb_v2.webp",
                null,
                1,
                false,
                null,
                "designtouch/thumbnails/thumb_v2"
        );

        mockMvc.perform(put("/api/portfolio/" + projectId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagePublicId").value("designtouch/thumbnails/thumb_v2"));

        // Verify old image was deleted from Cloudinary
        verify(mediaService).deleteImage("designtouch/thumbnails/thumb_v1");

        // 3. Delete project
        mockMvc.perform(delete("/api/portfolio/" + projectId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify current image was deleted on project deletion
        verify(mediaService).deleteImage("designtouch/thumbnails/thumb_v2");
    }

    @Test
    void testCreatePortfolio_videoWithDynamicCategory_persistsSuccessfully() throws Exception {
        // Find or create a video category with a slug that is NOT in PortfolioCategory enum
        com.designtouch.backend.entity.PortfolioCategory dynCategory = categoryRepository
                .findBySlugAndCategoryType("dynamic-batch", com.designtouch.backend.entity.enums.CategoryType.VIDEO_EDITING)
                .orElseGet(() -> categoryRepository.save(com.designtouch.backend.entity.PortfolioCategory.builder()
                        .name("Dynamic Batch")
                        .slug("dynamic-batch")
                        .categoryType(com.designtouch.backend.entity.enums.CategoryType.VIDEO_EDITING)
                        .isActive(true)
                        .displayOrder(99)
                        .build()));

        // Payload with unknown category string (simulating frontend sending dynamic category)
        String jsonPayload = """
                {
                    "title": "Dynamic Batch Showreel",
                    "contentType": "VIDEO_EDITING",
                    "categoryId": "%s",
                    "category": "DYNAMIC_BATCH_UNMAPPED",
                    "videoUrl": "https://res.cloudinary.com/demo/video/upload/v1/designtouch/portfolio/showreel.mp4",
                    "videoPublicId": "designtouch/portfolio/showreel",
                    "imageUrl": "https://res.cloudinary.com/demo/image/upload/v1/designtouch/portfolio/showreel_poster.jpg",
                    "imagePublicId": "designtouch/portfolio/showreel_poster",
                    "duration": "01:45",
                    "displayOrder": 1,
                    "isFeatured": true
                }
                """.formatted(dynCategory.getId());

        String createRes = mockMvc.perform(post("/api/portfolio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Dynamic Batch Showreel"))
                .andExpect(jsonPath("$.categoryId").value(dynCategory.getId().toString()))
                .andExpect(jsonPath("$.videoUrl").value("https://res.cloudinary.com/demo/video/upload/v1/designtouch/portfolio/showreel.mp4"))
                .andExpect(jsonPath("$.videoPublicId").value("designtouch/portfolio/showreel"))
                .andReturn().getResponse().getContentAsString();

        String projectId = objectMapper.readTree(createRes).get("id").asText();

        // Verify retrieval via admin all endpoint
        mockMvc.perform(get("/api/portfolio/admin/all")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + projectId + "')].categoryId").value(dynCategory.getId().toString()));

        // Clean up
        mockMvc.perform(delete("/api/portfolio/" + projectId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testCreatePortfolio_videoWithLiveVideoUrlOnly_persistsAndResolvesBySlugAndUuid() throws Exception {
        String jsonPayload = """
                {
                    "title": "Nike Live Commercial 2026",
                    "contentType": "VIDEO_EDITING",
                    "description": "High-octane commercial edit for social live rollout",
                    "liveVideoUrl": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    "googleDriveUrl": "https://drive.google.com/drive/folders/test-folder-123",
                    "resourceLinks": [
                        {
                            "title": "9:16 Instagram Cut",
                            "url": "https://www.instagram.com/reel/example123",
                            "description": "Optimized vertical export for mobile stories"
                        },
                        {
                            "title": "4K ProRes Client Master",
                            "url": "https://drive.google.com/file/d/masterfile123",
                            "description": "Uncompressed ProRes 422 HQ master deliverable"
                        }
                    ],
                    "displayOrder": 1,
                    "isFeatured": true
                }
                """;

        String createRes = mockMvc.perform(post("/api/portfolio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Nike Live Commercial 2026"))
                .andExpect(jsonPath("$.slug").isNotEmpty())
                .andExpect(jsonPath("$.liveVideoUrl").value("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
                .andExpect(jsonPath("$.googleDriveUrl").value("https://drive.google.com/drive/folders/test-folder-123"))
                .andExpect(jsonPath("$.resourceLinks").isArray())
                .andExpect(jsonPath("$.resourceLinks[0].title").value("9:16 Instagram Cut"))
                .andExpect(jsonPath("$.resourceLinks[0].url").value("https://www.instagram.com/reel/example123"))
                .andExpect(jsonPath("$.resourceLinks[1].title").value("4K ProRes Client Master"))
                .andReturn().getResponse().getContentAsString();

        String projectId = objectMapper.readTree(createRes).get("id").asText();
        String slug = objectMapper.readTree(createRes).get("slug").asText();

        // 1. Resolve via public single endpoint by UUID
        mockMvc.perform(get("/api/portfolio/" + projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId))
                .andExpect(jsonPath("$.slug").value(slug))
                .andExpect(jsonPath("$.liveVideoUrl").value("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
                .andExpect(jsonPath("$.googleDriveUrl").value("https://drive.google.com/drive/folders/test-folder-123"))
                .andExpect(jsonPath("$.resourceLinks.length()").value(2));

        // 2. Resolve via public single endpoint by SLUG
        mockMvc.perform(get("/api/portfolio/" + slug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId))
                .andExpect(jsonPath("$.slug").value(slug))
                .andExpect(jsonPath("$.title").value("Nike Live Commercial 2026"));

        // Clean up
        mockMvc.perform(delete("/api/portfolio/" + projectId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testCreatePortfolio_invalidExternalUrls_rejectedWithBadRequest() throws Exception {
        // Dangerous/invalid Live Video URL
        String invalidLiveUrlPayload = """
                {
                    "title": "Exploit Attempt Project",
                    "contentType": "VIDEO_EDITING",
                    "liveVideoUrl": "javascript:alert('pwned')"
                }
                """;

        mockMvc.perform(post("/api/portfolio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLiveUrlPayload))
                .andExpect(status().isBadRequest());

        // Dangerous/invalid Google Drive URL
        String invalidDrivePayload = """
                {
                    "title": "Exploit Attempt Project 2",
                    "contentType": "VIDEO_EDITING",
                    "liveVideoUrl": "https://www.youtube.com/watch?v=valid",
                    "googleDriveUrl": "data:text/html;base64,PHNjcmlwdD4="
                }
                """;

        mockMvc.perform(post("/api/portfolio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDrivePayload))
                .andExpect(status().isBadRequest());

        // Invalid resource link (missing title or invalid protocol)
        String invalidResourcePayload = """
                {
                    "title": "Exploit Attempt Project 3",
                    "contentType": "VIDEO_EDITING",
                    "liveVideoUrl": "https://www.youtube.com/watch?v=valid",
                    "resourceLinks": [
                        {
                            "title": "",
                            "url": "https://valid.com"
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/api/portfolio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidResourcePayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testVideoAspectRatio_allFormats_persistAndReturnCorrectly() throws Exception {
        String[] formats = new String[]{"9:16", "16:9", "1:1", "4:5", "AUTO"};

        for (String fmt : formats) {
            String payload = """
                    {
                        "title": "Ratio Test %s",
                        "contentType": "VIDEO_EDITING",
                        "videoUrl": "https://res.cloudinary.com/demo/video/upload/sample.mp4",
                        "videoAspectRatio": "%s"
                    }
                    """.formatted(fmt, fmt);

            String res = mockMvc.perform(post("/api/portfolio")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.videoAspectRatio").value(fmt))
                    .andReturn().getResponse().getContentAsString();

            String id = objectMapper.readTree(res).get("id").asText();

            // Verify get by ID
            mockMvc.perform(get("/api/portfolio/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.videoAspectRatio").value(fmt));

            // Clean up
            mockMvc.perform(delete("/api/portfolio/" + id)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());
        }
    }

    @Test
    void testVideoAspectRatio_updateRatio_andAutoFallback() throws Exception {
        // 1. Create with 9:16
        String payload = """
                {
                    "title": "Vertical Reel Project",
                    "contentType": "VIDEO_EDITING",
                    "videoUrl": "https://res.cloudinary.com/demo/video/upload/reel.mp4",
                    "videoAspectRatio": "9:16"
                }
                """;

        String res = mockMvc.perform(post("/api/portfolio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.videoAspectRatio").value("9:16"))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(res).get("id").asText();

        // 2. Update to 1:1
        String updatePayload = """
                {
                    "title": "Vertical Reel Project Updated",
                    "contentType": "VIDEO_EDITING",
                    "videoUrl": "https://res.cloudinary.com/demo/video/upload/reel.mp4",
                    "videoAspectRatio": "1:1"
                }
                """;

        mockMvc.perform(put("/api/portfolio/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.videoAspectRatio").value("1:1"));

        // Clean up
        mockMvc.perform(delete("/api/portfolio/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testVideoAspectRatio_thumbnailHasNullRatio_andInvalidRatioRejected() throws Exception {
        // Thumbnail design should have null ratio
        String thumbPayload = """
                {
                    "title": "Thumb Project No Ratio",
                    "contentType": "THUMBNAIL_DESIGN",
                    "imageUrl": "https://res.cloudinary.com/demo/image/upload/sample.jpg"
                }
                """;

        String res = mockMvc.perform(post("/api/portfolio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(thumbPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.videoAspectRatio").isEmpty())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(res).get("id").asText();
        mockMvc.perform(delete("/api/portfolio/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Invalid ratio string
        String invalidRatioPayload = """
                {
                    "title": "Invalid Ratio Project",
                    "contentType": "VIDEO_EDITING",
                    "videoUrl": "https://res.cloudinary.com/demo/video/upload/sample.mp4",
                    "videoAspectRatio": "99:1"
                }
                """;

        mockMvc.perform(post("/api/portfolio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRatioPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetFeatured_allAndByContentType() throws Exception {
        // 1. Get all featured
        mockMvc.perform(get("/api/portfolio/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 2. Get featured videos
        mockMvc.perform(get("/api/portfolio/featured?contentType=VIDEO_EDITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.contentType != 'VIDEO_EDITING')]").doesNotExist());

        // 3. Get featured thumbnails
        mockMvc.perform(get("/api/portfolio/featured?contentType=THUMBNAIL_DESIGN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.contentType != 'THUMBNAIL_DESIGN')]").doesNotExist());
    }

    @Test
    void testThumbnailProject_withGoogleDriveUrl_createUpdateAndValidate() throws Exception {
        // 1. Create Thumbnail Project with Google Drive URL
        String payload = """
                {
                    "title": "Thumb Drive Test",
                    "contentType": "THUMBNAIL_DESIGN",
                    "imageUrl": "https://res.cloudinary.com/demo/image/upload/sample.jpg",
                    "googleDriveUrl": "https://drive.google.com/drive/folders/1ABC_xyz-folder123"
                }
                """;

        String res = mockMvc.perform(post("/api/portfolio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.googleDriveUrl").value("https://drive.google.com/drive/folders/1ABC_xyz-folder123"))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(res).get("id").asText();

        // 2. Update with docs.google.com URL
        String updatePayload = """
                {
                    "title": "Thumb Drive Test Updated",
                    "contentType": "THUMBNAIL_DESIGN",
                    "imageUrl": "https://res.cloudinary.com/demo/image/upload/sample.jpg",
                    "googleDriveUrl": "https://docs.google.com/presentation/d/sample-doc"
                }
                """;

        mockMvc.perform(put("/api/portfolio/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.googleDriveUrl").value("https://docs.google.com/presentation/d/sample-doc"));

        // 3. Update with empty string to clear googleDriveUrl
        String clearPayload = """
                {
                    "title": "Thumb Drive Test Cleared",
                    "contentType": "THUMBNAIL_DESIGN",
                    "imageUrl": "https://res.cloudinary.com/demo/image/upload/sample.jpg",
                    "googleDriveUrl": ""
                }
                """;

        mockMvc.perform(put("/api/portfolio/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clearPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.googleDriveUrl").isEmpty());

        // 4. Clean up
        mockMvc.perform(delete("/api/portfolio/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGoogleDriveUrl_validation_rejectsInvalidDomainsAndProtocols() throws Exception {
        // HTTP instead of HTTPS
        String nonHttpsPayload = """
                {
                    "title": "Invalid Insecure URL",
                    "contentType": "THUMBNAIL_DESIGN",
                    "imageUrl": "https://res.cloudinary.com/demo/image/upload/sample.jpg",
                    "googleDriveUrl": "http://drive.google.com/drive/folders/123"
                }
                """;
        mockMvc.perform(post("/api/portfolio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nonHttpsPayload))
                .andExpect(status().isBadRequest());

        // Non-google drive domain
        String maliciousDomainPayload = """
                {
                    "title": "Malicious Domain URL",
                    "contentType": "THUMBNAIL_DESIGN",
                    "imageUrl": "https://res.cloudinary.com/demo/image/upload/sample.jpg",
                    "googleDriveUrl": "https://evil-site.com/drive.google.com"
                }
                """;
        mockMvc.perform(post("/api/portfolio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(maliciousDomainPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDownloadImageEndpoint_noImageOrNotFound_returnsNotFound() throws Exception {
        // Non-existent project
        mockMvc.perform(get("/api/portfolio/non-existent-project-slug/download-image"))
                .andExpect(status().isNotFound());
    }
}

