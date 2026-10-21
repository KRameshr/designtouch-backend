package com.designtouch.backend;

import com.designtouch.backend.dto.PortfolioCategoryRequest;
import com.designtouch.backend.dto.PortfolioItemRequest;
import com.designtouch.backend.entity.AdminUser;
import com.designtouch.backend.entity.PortfolioCategory;
import com.designtouch.backend.entity.PortfolioItem;
import com.designtouch.backend.entity.enums.CategoryType;
import com.designtouch.backend.entity.enums.ContentType;
import com.designtouch.backend.repository.AdminUserRepository;
import com.designtouch.backend.repository.PortfolioCategoryRepository;
import com.designtouch.backend.repository.PortfolioItemRepository;
import com.designtouch.backend.security.JwtService;
import com.designtouch.backend.service.MediaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PortfolioCategoryIntegrationTest {

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

    @MockBean
    private MediaService mediaService;

    private String adminToken;

    @BeforeEach
    void setUp() {
        AdminUser admin = adminUserRepository.findAll().stream().findFirst().orElseThrow();
        adminToken = jwtService.generateToken(admin.getUsername());
    }

    private void cleanupTestArtifacts() {
        for (String title : java.util.List.of("Deep Dive Episode 1", "Fortnite Tournament Thumbnail", "Category Switch Test", "Seed Project for Category Usage")) {
            itemRepository.findAll().stream()
                    .filter(i -> title.equals(i.getTitle()))
                    .forEach(itemRepository::delete);
        }
        for (String slug : java.util.List.of("podcast", "creator-podcast", "gaming", "pro-gaming", "temporary-category", "sneak-peek")) {
            categoryRepository.findAll().stream()
                    .filter(c -> slug.equals(c.getSlug()))
                    .forEach(categoryRepository::delete);
        }
    }

    // 1. Public GET categories
    @Test
    @Order(1)
    void testPublicGetCategories() throws Exception {
        cleanupTestArtifacts();
        mockMvc.perform(get("/api/portfolio-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(11))));
    }

    // 2. Create Video Editing category
    @Test
    @Order(2)
    void testCreateVideoCategory() throws Exception {
        PortfolioCategoryRequest req = new PortfolioCategoryRequest(
                "Podcast",
                "podcast",
                CategoryType.VIDEO_EDITING,
                7,
                true
        );

        mockMvc.perform(post("/api/portfolio-categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Podcast"))
                .andExpect(jsonPath("$.slug").value("podcast"))
                .andExpect(jsonPath("$.categoryType").value("VIDEO_EDITING"))
                .andExpect(jsonPath("$.displayOrder").value(7))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    // 3. Create Thumbnail Design category
    @Test
    @Order(3)
    void testCreateThumbnailCategory() throws Exception {
        PortfolioCategoryRequest req = new PortfolioCategoryRequest(
                "Gaming",
                "gaming",
                CategoryType.THUMBNAIL_DESIGN,
                6,
                true
        );

        mockMvc.perform(post("/api/portfolio-categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Gaming"))
                .andExpect(jsonPath("$.slug").value("gaming"))
                .andExpect(jsonPath("$.categoryType").value("THUMBNAIL_DESIGN"))
                .andExpect(jsonPath("$.displayOrder").value(6))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    // 4. Update Video category
    @Test
    @Order(4)
    void testUpdateVideoCategory() throws Exception {
        PortfolioCategory podcast = categoryRepository.findBySlugAndCategoryType("podcast", CategoryType.VIDEO_EDITING).orElseThrow();

        PortfolioCategoryRequest updateReq = new PortfolioCategoryRequest(
                "Creator Podcast",
                "creator-podcast",
                CategoryType.VIDEO_EDITING,
                8,
                true
        );

        mockMvc.perform(put("/api/portfolio-categories/" + podcast.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Creator Podcast"))
                .andExpect(jsonPath("$.slug").value("creator-podcast"))
                .andExpect(jsonPath("$.displayOrder").value(8));
    }

    // 5. Update Thumbnail category
    @Test
    @Order(5)
    void testUpdateThumbnailCategory() throws Exception {
        PortfolioCategory gaming = categoryRepository.findBySlugAndCategoryType("gaming", CategoryType.THUMBNAIL_DESIGN).orElseThrow();

        PortfolioCategoryRequest updateReq = new PortfolioCategoryRequest(
                "Pro Gaming",
                "pro-gaming",
                CategoryType.THUMBNAIL_DESIGN,
                9,
                true
        );

        mockMvc.perform(put("/api/portfolio-categories/" + gaming.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pro Gaming"))
                .andExpect(jsonPath("$.slug").value("pro-gaming"))
                .andExpect(jsonPath("$.displayOrder").value(9));
    }

    // 6. Delete unused category
    @Test
    @Order(6)
    void testDeleteUnusedCategory() throws Exception {
        // Create an unused category
        PortfolioCategory unused = categoryRepository.save(PortfolioCategory.builder()
                .name("Temporary Category")
                .slug("temporary-category")
                .categoryType(CategoryType.VIDEO_EDITING)
                .displayOrder(99)
                .isActive(true)
                .build());

        mockMvc.perform(delete("/api/portfolio-categories/" + unused.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertFalse(categoryRepository.existsById(unused.getId()));
    }

    // 7. Block deletion of used category
    @Test
    @Order(7)
    void testBlockDeletionOfUsedCategory() throws Exception {
        PortfolioCategory youtube = categoryRepository.findBySlugAndCategoryType("youtube", CategoryType.VIDEO_EDITING).orElseThrow();

        // Ensure there is at least one item using youtube
        long count = itemRepository.countByCategoryRef_Id(youtube.getId());
        if (count == 0) {
            itemRepository.save(PortfolioItem.builder()
                    .title("Seed Project for Category Usage")
                    .contentType(ContentType.VIDEO_EDITING)
                    .categoryRef(youtube)
                    .displayOrder(0)
                    .isFeatured(false)
                    .build());
        }

        mockMvc.perform(delete("/api/portfolio-categories/" + youtube.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("This category is currently used by")));

        assertTrue(categoryRepository.existsById(youtube.getId()));
    }

    // 8. Duplicate category rejection
    @Test
    @Order(8)
    void testDuplicateCategoryRejection() throws Exception {
        PortfolioCategoryRequest dupReq = new PortfolioCategoryRequest(
                "YouTube",
                "youtube",
                CategoryType.VIDEO_EDITING,
                1,
                true
        );

        mockMvc.perform(post("/api/portfolio-categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dupReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    // 9. Unauthorized create
    @Test
    @Order(9)
    void testUnauthorizedCreate() throws Exception {
        PortfolioCategoryRequest req = new PortfolioCategoryRequest(
                "Sneak Peek",
                "sneak-peek",
                CategoryType.VIDEO_EDITING,
                10,
                true
        );

        mockMvc.perform(post("/api/portfolio-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // 10. Unauthorized update
    @Test
    @Order(10)
    void testUnauthorizedUpdate() throws Exception {
        PortfolioCategory youtube = categoryRepository.findBySlugAndCategoryType("youtube", CategoryType.VIDEO_EDITING).orElseThrow();

        PortfolioCategoryRequest req = new PortfolioCategoryRequest(
                "YouTube Updated",
                "youtube",
                CategoryType.VIDEO_EDITING,
                1,
                true
        );

        mockMvc.perform(put("/api/portfolio-categories/" + youtube.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // 11. Unauthorized delete
    @Test
    @Order(11)
    void testUnauthorizedDelete() throws Exception {
        PortfolioCategory youtube = categoryRepository.findBySlugAndCategoryType("youtube", CategoryType.VIDEO_EDITING).orElseThrow();

        mockMvc.perform(delete("/api/portfolio-categories/" + youtube.getId()))
                .andExpect(status().isForbidden());
    }

    // 12. Existing portfolio records still load
    @Test
    @Order(12)
    void testExistingPortfolioRecordsStillLoad() throws Exception {
        mockMvc.perform(get("/api/portfolio?contentType=VIDEO_EDITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title").isNotEmpty())
                .andExpect(jsonPath("$[0].contentType").value("VIDEO_EDITING"));
    }

    // 13. Create Video project with dynamic category
    @Test
    @Order(13)
    void testCreateVideoProjectWithDynamicCategory() throws Exception {
        PortfolioCategory podcast = categoryRepository.findBySlugAndCategoryType("creator-podcast", CategoryType.VIDEO_EDITING)
                .or(() -> categoryRepository.findBySlugAndCategoryType("podcast", CategoryType.VIDEO_EDITING))
                .orElseThrow();

        PortfolioItemRequest itemReq = new PortfolioItemRequest(
                "Deep Dive Episode 1",
                ContentType.VIDEO_EDITING,
                null,
                null,
                "Multi-cam podcast edit",
                "45:00",
                "https://res.cloudinary.com/demo/video/upload/sample.mp4",
                null,
                null,
                "designtouch/portfolio/podcast1",
                10,
                true,
                podcast.getId()
        );

        mockMvc.perform(post("/api/portfolio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Deep Dive Episode 1"))
                .andExpect(jsonPath("$.categoryId").value(podcast.getId().toString()))
                .andExpect(jsonPath("$.categoryName").value(podcast.getName()))
                .andExpect(jsonPath("$.categorySlug").value(podcast.getSlug()))
                .andExpect(jsonPath("$.categoryType").value("VIDEO_EDITING"));
    }

    // 14. Create Thumbnail project with dynamic category
    @Test
    @Order(14)
    void testCreateThumbnailProjectWithDynamicCategory() throws Exception {
        PortfolioCategory gaming = categoryRepository.findBySlugAndCategoryType("pro-gaming", CategoryType.THUMBNAIL_DESIGN)
                .or(() -> categoryRepository.findBySlugAndCategoryType("gaming", CategoryType.THUMBNAIL_DESIGN))
                .orElseThrow();

        PortfolioItemRequest itemReq = new PortfolioItemRequest(
                "Fortnite Tournament Thumbnail",
                ContentType.THUMBNAIL_DESIGN,
                null,
                null,
                "High CTR packaging with 3D render",
                null,
                null,
                "https://placehold.co/1280x720.png",
                "https://placehold.co/1280x720.png",
                null,
                5,
                false,
                gaming.getId()
        );

        mockMvc.perform(post("/api/portfolio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Fortnite Tournament Thumbnail"))
                .andExpect(jsonPath("$.categoryId").value(gaming.getId().toString()))
                .andExpect(jsonPath("$.categoryName").value(gaming.getName()))
                .andExpect(jsonPath("$.categorySlug").value(gaming.getSlug()))
                .andExpect(jsonPath("$.categoryType").value("THUMBNAIL_DESIGN"));
    }

    // 15. Update project category
    @Test
    @Order(15)
    void testUpdateProjectCategory() throws Exception {
        PortfolioCategory youtube = categoryRepository.findBySlugAndCategoryType("youtube", CategoryType.VIDEO_EDITING).orElseThrow();
        PortfolioCategory commercial = categoryRepository.findBySlugAndCategoryType("commercial", CategoryType.VIDEO_EDITING).orElseThrow();

        // Create item with youtube
        PortfolioItem item = itemRepository.save(PortfolioItem.builder()
                .title("Category Switch Test")
                .contentType(ContentType.VIDEO_EDITING)
                .categoryRef(youtube)
                .videoUrl("https://res.cloudinary.com/demo/video/upload/sample.mp4")
                .displayOrder(1)
                .isFeatured(false)
                .build());

        // Update item to commercial
        PortfolioItemRequest updateReq = new PortfolioItemRequest(
                "Category Switch Test",
                ContentType.VIDEO_EDITING,
                null,
                null,
                "Description",
                "01:00",
                "https://res.cloudinary.com/demo/video/upload/sample.mp4",
                null,
                null,
                null,
                1,
                false,
                commercial.getId()
        );

        mockMvc.perform(put("/api/portfolio/" + item.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(commercial.getId().toString()))
                .andExpect(jsonPath("$.categoryName").value("Commercial"))
                .andExpect(jsonPath("$.categorySlug").value("commercial"));
    }

    // 16. Correct Video category filtering
    @Test
    @Order(16)
    void testVideoCategoryFiltering() throws Exception {
        PortfolioCategory youtube = categoryRepository.findBySlugAndCategoryType("youtube", CategoryType.VIDEO_EDITING).orElseThrow();

        mockMvc.perform(get("/api/portfolio?contentType=VIDEO_EDITING&categoryId=" + youtube.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].categoryId").value(youtube.getId().toString()));
    }

    // 17. Correct Thumbnail category filtering
    @Test
    @Order(17)
    void testThumbnailCategoryFiltering() throws Exception {
        PortfolioCategory gaming = categoryRepository.findBySlugAndCategoryType("pro-gaming", CategoryType.THUMBNAIL_DESIGN)
                .or(() -> categoryRepository.findBySlugAndCategoryType("gaming", CategoryType.THUMBNAIL_DESIGN))
                .orElseThrow();

        mockMvc.perform(get("/api/portfolio?contentType=THUMBNAIL_DESIGN&categoryId=" + gaming.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].categoryId").value(gaming.getId().toString()));
        cleanupTestArtifacts();
    }
}

