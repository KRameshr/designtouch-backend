package com.designtouch.backend;

import com.designtouch.backend.dto.ProcessStepRequest;
import com.designtouch.backend.dto.ServiceRequest;
import com.designtouch.backend.dto.ServicesHeroBannerRequest;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
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
                true, true, null, null, "Services Eyebrow", "Services Heading", "Services Desc",
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
                true, true, null, null, "OUR SERVICES", "DISCIPLINES", "Full scope of work",
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

        // Verify saving text fields without banner fields keeps existing Cloudinary URL and public ID intact
        ServicesPageContentRequest textOnlyUpdate = new ServicesPageContentRequest(
                "NEW EYEBROW", "BANNER LINE 1", "BANNER LINE 2", "HIGHLIGHT",
                "Description", "CTA", "/contact", "CTA 2", "/portfolio",
                null, "10+", "Stats", "20+", "Stats 2", "30+", "Stats 3",
                true, true, null, null,
                "Services Eyebrow", "Services Heading", "Services Desc",
                "Process Eyebrow", "Process Heading", "Process Desc",
                "Tools Eyebrow", "Tools Heading", "Tools Desc",
                "CTA Eyebrow", "CTA Line 1", "CTA Line 2", "CTA Highlight",
                "CTA Desc", "Button", "/contact", "Accent Note"
        );

        mockMvc.perform(put("/api/services-page")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(textOnlyUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heroEyebrow").value("NEW EYEBROW"))
                .andExpect(jsonPath("$.heroBannerImageUrl").value("https://res.cloudinary.com/demo/image/upload/v1234567/designtouch/services/banner.webp"))
                .andExpect(jsonPath("$.heroBannerImagePublicId").value("designtouch/services/banner"));

        // Verify public GET returns the updated banner image without authentication
        mockMvc.perform(get("/api/services-page"))
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
    void testMultiBannerLifecycle_maxFiveActiveOrderDelete() throws Exception {
        // Unauthenticated admin list should be forbidden
        mockMvc.perform(get("/api/services-page/banners/admin/all"))
                .andExpect(status().isForbidden());

        // Start from a clean banner state inside this rollback transaction
        mockMvc.perform(delete("/api/services-page/hero-banner")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Add 5 banners (Banner 1 through Banner 5)
        String[] bannerIds = new String[5];
        for (int i = 1; i <= 5; i++) {
            ServicesHeroBannerRequest req = new ServicesHeroBannerRequest(
                    "Banner " + i,
                    "https://res.cloudinary.com/demo/image/upload/v1/designtouch/services/b" + i + ".png",
                    "designtouch/services/b" + i,
                    i,
                    true
            );
            String res = mockMvc.perform(post("/api/services-page/banners")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.label").value("Banner " + i))
                    .andExpect(jsonPath("$.displayOrder").value(i))
                    .andExpect(jsonPath("$.isActive").value(true))
                    .andReturn().getResponse().getContentAsString();
            bannerIds[i - 1] = objectMapper.readTree(res).get("id").asText();
        }

        // Attempting to add a 6th banner must be rejected with 400 Bad Request
        ServicesHeroBannerRequest sixth = new ServicesHeroBannerRequest(
                "Banner 6",
                "https://res.cloudinary.com/demo/image/upload/v1/designtouch/services/b6.png",
                "designtouch/services/b6",
                6,
                true
        );
        mockMvc.perform(post("/api/services-page/banners")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sixth)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Maximum of 5 hero banners allowed. Please delete or replace an existing banner."));

        // Public GET /api/services-page/banners returns all 5 active banners in displayOrder
        mockMvc.perform(get("/api/services-page/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].label").value("Banner 1"))
                .andExpect(jsonPath("$[4].label").value("Banner 5"));

        // Deactivate Banner 2
        mockMvc.perform(patch("/api/services-page/banners/" + bannerIds[1] + "/toggle-active")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        // Public GET now returns 4 active banners (excluding Banner 2)
        mockMvc.perform(get("/api/services-page/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[?(@.label == 'Banner 2')]").doesNotExist());

        // Admin GET still returns all 5 banners
        mockMvc.perform(get("/api/services-page/banners/admin/all")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));

        // Update Banner 1's image and displayOrder
        ServicesHeroBannerRequest updateB1 = new ServicesHeroBannerRequest(
                "Banner 1 Updated",
                "https://res.cloudinary.com/demo/image/upload/v1/designtouch/services/b1_new.png",
                "designtouch/services/b1_new",
                10,
                true
        );
        mockMvc.perform(put("/api/services-page/banners/" + bannerIds[0])
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateB1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Banner 1 Updated"))
                .andExpect(jsonPath("$.imageUrl").value("https://res.cloudinary.com/demo/image/upload/v1/designtouch/services/b1_new.png"));

        // Verify Banner 1 Updated is now last in public active sequence due to displayOrder = 10
        mockMvc.perform(get("/api/services-page/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("Banner 3"))
                .andExpect(jsonPath("$[3].label").value("Banner 1 Updated"));

        // Delete Banner 5 and verify remaining 4 banners work
        mockMvc.perform(delete("/api/services-page/banners/" + bannerIds[4])
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/services-page/banners/admin/all")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
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
                "discover",
                9,
                true
        );

        String createRes = mockMvc.perform(post("/api/process-steps")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Step Launch"))
                .andExpect(jsonPath("$.icon").value("discover"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andReturn().getResponse().getContentAsString();

        String stepId = objectMapper.readTree(createRes).get("id").asText();

        // Update icon selection to 'plan' and verify public endpoint reflects the saved icon key
        ProcessStepRequest updateReq = new ProcessStepRequest(
                PageType.SERVICES,
                (short) 9,
                "Test Step Launch Updated",
                "Updated step description",
                "plan",
                9,
                true
        );

        mockMvc.perform(put("/api/process-steps/" + stepId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Step Launch Updated"))
                .andExpect(jsonPath("$.icon").value("plan"));

        mockMvc.perform(get("/api/process-steps").param("pageType", "SERVICES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + stepId + "')].icon").value("plan"));

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
                "3D",
                "/assets/tools/blender.svg",
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
                .andExpect(jsonPath("$.logoUrl").value("/assets/tools/blender.svg"))
                .andExpect(jsonPath("$.role").value("3D Motion Suite"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andReturn().getResponse().getContentAsString();

        String toolId = objectMapper.readTree(createRes).get("id").asText();

        // Update tool icon selection to '/assets/tools/figma.svg' and verify other fields remain intact
        ToolRequest updateReq = new ToolRequest(
                "Test Cinema4D",
                "Fg",
                "/assets/tools/figma.svg",
                "https://maxon.net",
                "3D Motion Suite",
                10,
                true
        );

        mockMvc.perform(put("/api/tools/" + toolId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Cinema4D"))
                .andExpect(jsonPath("$.logoUrl").value("/assets/tools/figma.svg"))
                .andExpect(jsonPath("$.role").value("3D Motion Suite"))
                .andExpect(jsonPath("$.websiteUrl").value("https://maxon.net"));

        mockMvc.perform(get("/api/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + toolId + "')].logoUrl").value("/assets/tools/figma.svg"));

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

