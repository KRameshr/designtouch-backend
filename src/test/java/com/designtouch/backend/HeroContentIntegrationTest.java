package com.designtouch.backend;

import com.designtouch.backend.entity.AdminUser;
import com.designtouch.backend.entity.enums.AdminRole;
import com.designtouch.backend.repository.AdminUserRepository;
import com.designtouch.backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class HeroContentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Test
    void testGetHeroContentPublic() throws Exception {
        mockMvc.perform(get("/api/hero"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eyebrow").value("Welcome to DesignTouch+"))
                .andExpect(jsonPath("$.headlineLine1").value("We create"))
                .andExpect(jsonPath("$.headlineLine2").value("videos that"))
                .andExpect(jsonPath("$.headlineLine3").value("grow"))
                .andExpect(jsonPath("$.headlineHighlight").value("brands."))
                .andExpect(jsonPath("$.videoUrl").value("/hero-demo.mp4"));
    }

    @Test
    void testPutHeroContentUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(put("/api/hero")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"eyebrow\":\"Forbidden update\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testPutHeroContentAuthorizedWithToken() throws Exception {
        // Find existing admin or create a test admin
        AdminUser admin = adminUserRepository.findAll().stream().findFirst().orElseGet(() -> {
            AdminUser newAdmin = AdminUser.builder()
                    .username("test_admin")
                    .passwordHash("hash")
                    .role(AdminRole.SUPER_ADMIN)
                    .build();
            return adminUserRepository.save(newAdmin);
        });

        String token = jwtService.generateToken(admin.getUsername());

        String updateJson = """
            {
                "eyebrow": "Welcome to DesignTouch+ (Updated)",
                "headlineLine1": "We craft",
                "headlineLine2": "cinematic stories that",
                "headlineLine3": "scale",
                "headlineHighlight": "creators.",
                "subheading": "Premium video editing and storytelling.",
                "primaryCtaText": "Explore Work",
                "primaryCtaLink": "/portfolio",
                "secondaryCtaText": "Talk to Us",
                "secondaryCtaLink": "/contact",
                "videoUrl": "/hero-demo.mp4",
                "badgeText": "REEL // 2026",
                "specBadgeText": "4K PRORES"
            }
            """;

        mockMvc.perform(put("/api/hero")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eyebrow").value("Welcome to DesignTouch+ (Updated)"))
                .andExpect(jsonPath("$.headlineLine1").value("We craft"))
                .andExpect(jsonPath("$.headlineHighlight").value("creators."));

        // Revert back to original approved content so test leaves state clean
        String originalJson = """
            {
                "eyebrow": "Welcome to DesignTouch+",
                "headlineLine1": "We create",
                "headlineLine2": "videos that",
                "headlineLine3": "grow",
                "headlineHighlight": "brands.",
                "subheading": "We help businesses and creators tell their stories through powerful video editing & creative content.",
                "primaryCtaText": "View Portfolio",
                "primaryCtaLink": "/portfolio",
                "secondaryCtaText": "Book a Call",
                "secondaryCtaLink": "/contact",
                "videoUrl": "/hero-demo.mp4",
                "badgeText": "REEL // 2026",
                "specBadgeText": "4K PRORES"
            }
            """;

        mockMvc.perform(put("/api/hero")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(originalJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eyebrow").value("Welcome to DesignTouch+"));
    }
}

