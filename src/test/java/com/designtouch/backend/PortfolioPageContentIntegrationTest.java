package com.designtouch.backend;

import com.designtouch.backend.dto.PortfolioPageContentRequest;
import com.designtouch.backend.dto.PortfolioPageTagRequest;
import com.designtouch.backend.entity.AdminUser;
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
class PortfolioPageContentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdminUserRepository adminUserRepository;

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

    @Test
    void testGetPortfolioPageContent_public_success() throws Exception {
        mockMvc.perform(get("/api/portfolio-page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eyebrow").isNotEmpty())
                .andExpect(jsonPath("$.headlineLine1").isNotEmpty());
    }

    @Test
    void testUpdatePortfolioPageContent_unauthenticated_forbidden() throws Exception {
        PortfolioPageContentRequest request = new PortfolioPageContentRequest(
                "TEST EYEBROW", "Line 1", "Line 2", "Highlight", "Desc", null, null
        );

        mockMvc.perform(put("/api/portfolio-page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdatePortfolioPageContent_admin_success_andVideoLifecycle() throws Exception {
        // Step 1: Set intro video
        PortfolioPageContentRequest req1 = new PortfolioPageContentRequest(
                "OUR PORTFOLIO // SELECTED WORK",
                "Our Work",
                "Speaks",
                "Louder.",
                "Explore our curated portfolio.",
                "https://res.cloudinary.com/demo/video/upload/v1/designtouch/portfolio-intro/reel_v1.mp4",
                "designtouch/portfolio-intro/reel_v1"
        );

        mockMvc.perform(put("/api/portfolio-page")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.introVideoPublicId").value("designtouch/portfolio-intro/reel_v1"));

        // Step 2: Replace intro video
        PortfolioPageContentRequest req2 = new PortfolioPageContentRequest(
                "OUR PORTFOLIO // SELECTED WORK",
                "Our Work",
                "Speaks",
                "Louder.",
                "Explore our curated portfolio.",
                "https://res.cloudinary.com/demo/video/upload/v1/designtouch/portfolio-intro/reel_v2.mp4",
                "designtouch/portfolio-intro/reel_v2"
        );

        mockMvc.perform(put("/api/portfolio-page")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.introVideoPublicId").value("designtouch/portfolio-intro/reel_v2"));

        // Verify old video was deleted
        verify(mediaService).deleteVideo("designtouch/portfolio-intro/reel_v1");

        // Step 3: Remove intro video
        PortfolioPageContentRequest req3 = new PortfolioPageContentRequest(
                "OUR PORTFOLIO // SELECTED WORK",
                "Our Work",
                "Speaks",
                "Louder.",
                "Explore our curated portfolio.",
                null,
                null
        );

        mockMvc.perform(put("/api/portfolio-page")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.introVideoUrl").doesNotExist());

        // Verify reel_v2 was deleted when cleared
        verify(mediaService).deleteVideo("designtouch/portfolio-intro/reel_v2");
    }

    @Test
    void testTagsLifecycle_publicAndAdmin() throws Exception {
        // Public get tags
        mockMvc.perform(get("/api/portfolio-page/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Create tag unauthenticated -> forbidden
        PortfolioPageTagRequest tagReq = new PortfolioPageTagRequest("COLOR GRADING", 6, true);
        mockMvc.perform(post("/api/portfolio-page/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagReq)))
                .andExpect(status().isForbidden());

        // Create tag as admin -> 201 Created
        String res = mockMvc.perform(post("/api/portfolio-page/tags")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("COLOR GRADING"))
                .andReturn().getResponse().getContentAsString();

        String tagId = objectMapper.readTree(res).get("id").asText();

        // Update tag
        PortfolioPageTagRequest updateReq = new PortfolioPageTagRequest("PRO COLOR GRADING", 6, true);
        mockMvc.perform(put("/api/portfolio-page/tags/" + tagId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("PRO COLOR GRADING"));

        // Delete tag
        mockMvc.perform(delete("/api/portfolio-page/tags/" + tagId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}

