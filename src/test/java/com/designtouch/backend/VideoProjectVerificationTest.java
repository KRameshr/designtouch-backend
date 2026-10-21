package com.designtouch.backend;

import com.designtouch.backend.entity.PortfolioCategory;
import com.designtouch.backend.entity.PortfolioItem;
import com.designtouch.backend.entity.enums.CategoryType;
import com.designtouch.backend.repository.AdminUserRepository;
import com.designtouch.backend.repository.PortfolioCategoryRepository;
import com.designtouch.backend.repository.PortfolioItemRepository;
import com.designtouch.backend.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class VideoProjectVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PortfolioCategoryRepository categoryRepository;

    @Autowired
    private PortfolioItemRepository itemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() {
        var admin = adminUserRepository.findAll().stream().findFirst().orElseThrow();
        adminToken = jwtService.generateToken(admin.getUsername());
    }

    @Test
    void testSaveNewVideoProject_underNewBatchCategory_andVerifyAdminAndPublicDisplay() throws Exception {
        // Find existing "new bactch" category or create it
        PortfolioCategory category = categoryRepository.findBySlugAndCategoryType("new-bactch", CategoryType.VIDEO_EDITING)
                .orElseGet(() -> categoryRepository.save(PortfolioCategory.builder()
                        .name("new bactch")
                        .slug("new-bactch")
                        .categoryType(CategoryType.VIDEO_EDITING)
                        .isActive(true)
                        .displayOrder(0)
                        .build()));

        // 1. Simulate the exact frontend save payload for a Video Editing project
        String requestJson = """
                {
                    "title": "Neon Commercial Reel 2026",
                    "contentType": "VIDEO_EDITING",
                    "categoryId": "%s",
                    "category": null,
                    "subCategory": "Commercial 4K",
                    "description": "High end commercial reel with sound design and grading.",
                    "duration": "01:15",
                    "videoUrl": "https://res.cloudinary.com/dggikfrj5/video/upload/v1792494844/designtouch/portfolio/neon_reel.mp4",
                    "videoPublicId": "designtouch/portfolio/neon_reel",
                    "imageUrl": "https://res.cloudinary.com/dggikfrj5/image/upload/v1792494844/designtouch/portfolio/neon_reel_poster.jpg",
                    "imagePublicId": "designtouch/portfolio/neon_reel_poster",
                    "thumbnailUrl": null,
                    "displayOrder": 0,
                    "isFeatured": true
                }
                """.formatted(category.getId());

        String createRes = mockMvc.perform(post("/api/portfolio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Neon Commercial Reel 2026"))
                .andExpect(jsonPath("$.contentType").value("VIDEO_EDITING"))
                .andExpect(jsonPath("$.categoryId").value(category.getId().toString()))
                .andExpect(jsonPath("$.categoryName").value("new bactch"))
                .andExpect(jsonPath("$.videoUrl").value("https://res.cloudinary.com/dggikfrj5/video/upload/v1792494844/designtouch/portfolio/neon_reel.mp4"))
                .andExpect(jsonPath("$.videoPublicId").value("designtouch/portfolio/neon_reel"))
                .andExpect(jsonPath("$.imageUrl").value("https://res.cloudinary.com/dggikfrj5/image/upload/v1792494844/designtouch/portfolio/neon_reel_poster.jpg"))
                .andExpect(jsonPath("$.imagePublicId").value("designtouch/portfolio/neon_reel_poster"))
                .andReturn().getResponse().getContentAsString();

        JsonNode rootNode = objectMapper.readTree(createRes);
        UUID createdId = UUID.fromString(rootNode.get("id").asText());

        // 2. Verify project persists in database and is queryable directly
        PortfolioItem savedItem = itemRepository.findById(createdId).orElse(null);
        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getTitle()).isEqualTo("Neon Commercial Reel 2026");
        assertThat(savedItem.getCategoryRef().getId()).isEqualTo(category.getId());
        assertThat(savedItem.getVideoUrl()).contains("neon_reel.mp4");
        assertThat(savedItem.getVideoPublicId()).isEqualTo("designtouch/portfolio/neon_reel");
        assertThat(savedItem.getImageUrl()).contains("neon_reel_poster.jpg");

        // 3. Verify Admin list endpoint (/api/portfolio/admin/all) returns this project at the top
        String adminAllRes = mockMvc.perform(get("/api/portfolio/admin/all")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode adminArray = objectMapper.readTree(adminAllRes);
        assertThat(adminArray.isArray()).isTrue();
        assertThat(adminArray.get(0).get("id").asText()).isEqualTo(createdId.toString());
        assertThat(adminArray.get(0).get("categoryName").asText()).isEqualTo("new bactch");

        // 4. Verify Public portfolio endpoints:
        // A) Category filter by categoryId
        mockMvc.perform(get("/api/portfolio")
                        .param("contentType", "VIDEO_EDITING")
                        .param("categoryId", category.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + createdId + "')].title").value("Neon Commercial Reel 2026"))
                .andExpect(jsonPath("$[?(@.id == '" + createdId + "')].videoUrl").value("https://res.cloudinary.com/dggikfrj5/video/upload/v1792494844/designtouch/portfolio/neon_reel.mp4"));

        // B) Public Featured endpoint
        mockMvc.perform(get("/api/portfolio/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + createdId + "')].videoUrl").isNotEmpty());

        // 5. Clean up created project
        mockMvc.perform(delete("/api/portfolio/" + createdId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertThat(itemRepository.findById(createdId)).isEmpty();
    }

    @Test
    void testThumbnailDesignCreation_withoutVideo_persistsSuccessfully() throws Exception {
        PortfolioCategory thumbCat = categoryRepository.findBySlugAndCategoryType("motivation-content", CategoryType.THUMBNAIL_DESIGN)
                .orElseGet(() -> categoryRepository.save(PortfolioCategory.builder()
                        .name("Motivation Content")
                        .slug("motivation-content")
                        .categoryType(CategoryType.THUMBNAIL_DESIGN)
                        .isActive(true)
                        .displayOrder(1)
                        .build()));

        String thumbRequest = """
                {
                    "title": "10x YouTube Growth Thumbnail",
                    "contentType": "THUMBNAIL_DESIGN",
                    "categoryId": "%s",
                    "category": null,
                    "subCategory": "Motivation Content",
                    "description": "Bold typography and neon accents.",
                    "duration": null,
                    "videoUrl": null,
                    "videoPublicId": null,
                    "imageUrl": "https://res.cloudinary.com/dggikfrj5/image/upload/v1792494844/designtouch/thumbnails/growth_thumb.webp",
                    "imagePublicId": "designtouch/thumbnails/growth_thumb",
                    "thumbnailUrl": "https://res.cloudinary.com/dggikfrj5/image/upload/v1792494844/designtouch/thumbnails/growth_thumb.webp",
                    "displayOrder": 1,
                    "isFeatured": false
                }
                """.formatted(thumbCat.getId());

        String createRes = mockMvc.perform(post("/api/portfolio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(thumbRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("10x YouTube Growth Thumbnail"))
                .andExpect(jsonPath("$.contentType").value("THUMBNAIL_DESIGN"))
                .andExpect(jsonPath("$.videoUrl").isEmpty())
                .andExpect(jsonPath("$.imageUrl").value("https://res.cloudinary.com/dggikfrj5/image/upload/v1792494844/designtouch/thumbnails/growth_thumb.webp"))
                .andExpect(jsonPath("$.imagePublicId").value("designtouch/thumbnails/growth_thumb"))
                .andReturn().getResponse().getContentAsString();

        UUID thumbId = UUID.fromString(objectMapper.readTree(createRes).get("id").asText());

        // Clean up
        mockMvc.perform(delete("/api/portfolio/" + thumbId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}

