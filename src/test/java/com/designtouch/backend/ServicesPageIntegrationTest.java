package com.designtouch.backend;

import com.designtouch.backend.dto.ProcessStepRequest;
import com.designtouch.backend.dto.ServiceRequest;
import com.designtouch.backend.dto.ServicesPageContentRequest;
import com.designtouch.backend.dto.ToolRequest;
import com.designtouch.backend.entity.AdminUser;
import com.designtouch.backend.entity.enums.PageType;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ServicesPageIntegrationTest {

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
    void testGetServicesPageContent_public_success() throws Exception {
        mockMvc.perform(get("/api/services-page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heroEyebrow").isNotEmpty())
                .andExpect(jsonPath("$.heroHeadlineLine1").isNotEmpty());
    }

    @Test
    void testUpdateServicesPageContent_unauthenticated_forbidden() throws Exception {
        ServicesPageContentRequest request = new ServicesPageContentRequest(
                "TEST EYEBROW", "TEST LINE 1", "TEST LINE 2", "HIGHLIGHT",
                "Description test", "CTA", "/contact", "CTA 2", "/portfolio",
                null, "10+", "Stats", "20+", "Stats 2", "30+", "Stats 3",
                true, true, "Services Eyebrow", "Services Heading", "Services Desc",
                "Process Eyebrow", "Process Heading", "Process Desc",
                "Tools Eyebrow", "Tools Heading", "Tools Desc",
                "CTA Eyebrow", "CTA Line 1", "CTA Line 2", "CTA Highlight",
                "CTA Desc", "Button", "/contact", "Accent Note"
        );

        mockMvc.perform(put("/api/services-page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateServicesPageContent_authenticated_success() throws Exception {
        ServicesPageContentRequest request = new ServicesPageContentRequest(
                "UPDATED EYEBROW", "UPDATED LINE 1", "UPDATED LINE 2", "HIGHLIGHTED",
                "Updated description", "DISCUSS", "/contact", "PORTFOLIO", "/portfolio",
                "/hero-demo.mp4", "5K+", "Exports", "24H", "Turnaround", "99%", "Satisfaction",
                true, true, "OUR SERVICES", "DISCIPLINES", "Full scope of work",
                "HOW WE WORK", "OUR PROCESS", "Step by step execution",
                "PRODUCTION STACK", "TOOLS WE USE", "Pro tools description",
                "GET IN TOUCH", "LET'S BUILD", "SOMETHING", "ICONIC.",
                "Ready to start?", "START NOW", "/contact", "● NOW OPEN"
        );

        mockMvc.perform(put("/api/services-page")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heroEyebrow").value("UPDATED EYEBROW"))
                .andExpect(jsonPath("$.heroHeadlineHighlight").value("HIGHLIGHTED"))
                .andExpect(jsonPath("$.ctaHeadingHighlight").value("ICONIC."));
    }

    @Test
    void testUpdateServicesPageContent_withBannerImage_success() throws Exception {
        ServicesPageContentRequest request = new ServicesPageContentRequest(
                "BANNER EYEBROW", "BANNER LINE 1", "BANNER LINE 2", "HIGHLIGHT",
                "Description", "CTA", "/contact", "CTA 2", "/portfolio",
                null, "10+", "Stats", "20+", "Stats 2", "30+", "Stats 3",
                true, true,
                "https://res.cloudinary.com/demo/image/upload/v1234567/designtouch/services/banner.webp",
                "designtouch/services/banner",
                "Services Eyebrow", "Services Heading", "Services Desc",
                "Process Eyebrow", "Process Heading", "Process Desc",
                "Tools Eyebrow", "Tools Heading", "Tools Desc",
                "CTA Eyebrow", "CTA Line 1", "CTA Line 2", "CTA Highlight",
                "CTA Desc", "Button", "/contact", "Accent Note"
        );

        mockMvc.perform(put("/api/services-page")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heroBannerImageUrl").value("https://res.cloudinary.com/demo/image/upload/v1234567/designtouch/services/banner.webp"))
                .andExpect(jsonPath("$.heroBannerImagePublicId").value("designtouch/services/banner"));
    }

    @Test
    void testDeleteHeroBanner_unauthenticated_forbidden() throws Exception {
        mockMvc.perform(delete("/api/services-page/hero-banner"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteHeroBanner_authenticated_success() throws Exception {
        mockMvc.perform(delete("/api/services-page/hero-banner")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heroBannerImageUrl").isEmpty())
                .andExpect(jsonPath("$.heroBannerImagePublicId").isEmpty());
    }

    @Test
    void testServiceLifecycle_createUpdateToggleActiveDelete() throws Exception {
        // 1. Create a service
        ServiceRequest createReq = new ServiceRequest(
                "video",
                "Test Dynamic Service",
                "Original short description",
                "Original short description",
                "Full comprehensive description of the service",
                List.of("Feature 1", "Feature 2"),
                "red",
                "test-dynamic-service",
                "99",
                99,
                "Inquire Now",
                "/contact?service=Test",
                null,
                true
        );

        String createRes = mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Dynamic Service"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andReturn().getResponse().getContentAsString();

        String serviceId = objectMapper.readTree(createRes).get("id").asText();

        // 2. Toggle active (make it false)
        mockMvc.perform(patch("/api/services/" + serviceId + "/toggle-active")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        // Public GET /api/services should not include inactive
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + serviceId + "')]").doesNotExist());

        // Admin GET /api/services/admin/all should include it
        mockMvc.perform(get("/api/services/admin/all")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + serviceId + "')]").exists());

        // 3. Update service
        ServiceRequest updateReq = new ServiceRequest(
                "motion",
                "Test Dynamic Service Updated",
                "Updated short description",
                "Updated short description",
                "Updated full description",
                List.of("Feature A", "Feature B"),
                "green",
                "test-dynamic-service",
                "99",
                99,
                "Book Call",
                "/contact",
                null,
                true
        );

        mockMvc.perform(put("/api/services/" + serviceId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Dynamic Service Updated"))
                .andExpect(jsonPath("$.isActive").value(true));

        // 4. Delete service
        mockMvc.perform(delete("/api/services/" + serviceId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify gone
        mockMvc.perform(get("/api/services/admin/all")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$[?(@.id == '" + serviceId + "')]").doesNotExist());
    }

    @Test
    void testProcessStepLifecycle_createUpdateToggleActiveDelete() throws Exception {
        ProcessStepRequest createReq = new ProcessStepRequest(
                PageType.SERVICES,
                (short) 9,
                "Test Step Launch",
                "Test step description",
                "deliver",
                9,
                true
        );

        String createRes = mockMvc.perform(post("/api/process-steps")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Step Launch"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andReturn().getResponse().getContentAsString();

        String stepId = objectMapper.readTree(createRes).get("id").asText();

        // Toggle active
        mockMvc.perform(patch("/api/process-steps/" + stepId + "/toggle-active")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        // Delete
        mockMvc.perform(delete("/api/process-steps/" + stepId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testToolLifecycle_createUpdateToggleActiveDelete() throws Exception {
        ToolRequest createReq = new ToolRequest(
                "Test Cinema4D",
                "C4D",
                null,
                "https://maxon.net",
                "3D Motion Suite",
                10,
                true
        );

        String createRes = mockMvc.perform(post("/api/tools")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Cinema4D"))
                .andExpect(jsonPath("$.role").value("3D Motion Suite"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andReturn().getResponse().getContentAsString();

        String toolId = objectMapper.readTree(createRes).get("id").asText();

        // Toggle active
        mockMvc.perform(patch("/api/tools/" + toolId + "/toggle-active")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        // Delete
        mockMvc.perform(delete("/api/tools/" + toolId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}

