package com.designtouch.backend;

import com.designtouch.backend.dto.PricingHeroBannerRequest;
import com.designtouch.backend.dto.PricingPageContentRequest;
import com.designtouch.backend.entity.AdminUser;
import com.designtouch.backend.repository.AdminUserRepository;
import com.designtouch.backend.repository.PricingHeroBannerRepository;
import com.designtouch.backend.repository.PricingPageContentRepository;
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
class PricingHeroBannerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PricingHeroBannerRepository bannerRepository;

    @Autowired
    private PricingPageContentRepository pricingPageContentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MediaService mediaService;

    private String adminToken;

    @BeforeEach
    void setUp() {
        AdminUser admin = adminUserRepository.findByUsername("admin")
                .orElseGet(() -> adminUserRepository.save(AdminUser.builder()
                        .username("admin")
                        .passwordHash("$2a$10$dummyHashForTestingPurposesOnly")
                        .build()));
        adminToken = jwtService.generateToken(admin.getUsername());
        bannerRepository.deleteAll();
        bannerRepository.flush();
        pricingPageContentRepository.findById((short) 1).ifPresent(c -> {
            c.setHeroBannerImageUrl(null);
            c.setHeroBannerImagePublicId(null);
            c.setHeroBackgroundImageUrl(null);
            pricingPageContentRepository.saveAndFlush(c);
        });
    }

    @Test
    void testPricingHeroBannerCRUDAndMax5Constraint() throws Exception {
        String[] bannerIds = new String[5];
        for (int i = 1; i <= 5; i++) {
            PricingHeroBannerRequest req = new PricingHeroBannerRequest(
                    "Workstation " + i,
                    "https://res.cloudinary.com/demo/image/upload/v1/designtouch/pricing/b" + i + ".png",
                    "designtouch/pricing/b" + i,
                    "Production Studio Setup " + i,
                    "center",
                    i,
                    true
            );
            String res = mockMvc.perform(post("/api/pricing-page/banners")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.label").value("Workstation " + i))
                    .andExpect(jsonPath("$.displayOrder").value(i))
                    .andExpect(jsonPath("$.isActive").value(true))
                    .andExpect(jsonPath("$.altText").value("Production Studio Setup " + i))
                    .andReturn().getResponse().getContentAsString();
            bannerIds[i - 1] = objectMapper.readTree(res).get("id").asText();
        }

        // Attempting to add a 6th banner must be rejected with 400 Bad Request
        PricingHeroBannerRequest sixth = new PricingHeroBannerRequest(
                "Workstation 6",
                "https://res.cloudinary.com/demo/image/upload/v1/designtouch/pricing/b6.png",
                "designtouch/pricing/b6",
                "Alt 6",
                "center",
                6,
                true
        );
        mockMvc.perform(post("/api/pricing-page/banners")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sixth)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Maximum of 5 hero banners allowed. Please delete or replace an existing banner."));

        // Public GET /api/pricing-page/banners returns all 5 active banners in displayOrder
        mockMvc.perform(get("/api/pricing-page/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].label").value("Workstation 1"))
                .andExpect(jsonPath("$[4].label").value("Workstation 5"));

        // Deactivate Banner 2
        mockMvc.perform(patch("/api/pricing-page/banners/" + bannerIds[1] + "/toggle-active")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        // Public GET now returns 4 active banners
        mockMvc.perform(get("/api/pricing-page/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[?(@.label == 'Workstation 2')]").doesNotExist());

        // Admin GET still returns all 5 banners
        mockMvc.perform(get("/api/pricing-page/banners/admin/all")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));

        // Reorder banners (reverse order)
        List<String> reversedIds = List.of(bannerIds[4], bannerIds[3], bannerIds[2], bannerIds[1], bannerIds[0]);
        mockMvc.perform(put("/api/pricing-page/banners/reorder")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reversedIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bannerIds[4]))
                .andExpect(jsonPath("$[0].displayOrder").value(1));

        // Delete Banner 1
        mockMvc.perform(delete("/api/pricing-page/banners/" + bannerIds[0])
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Now total count is 4, adding a 5th banner succeeds
        PricingHeroBannerRequest newFifth = new PricingHeroBannerRequest(
                "Workstation Replacement",
                "https://res.cloudinary.com/demo/image/upload/v1/designtouch/pricing/new5.png",
                "designtouch/pricing/new5",
                "Replacement Alt",
                "top",
                5,
                true
        );
        mockMvc.perform(post("/api/pricing-page/banners")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newFifth)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("Workstation Replacement"))
                .andExpect(jsonPath("$.objectPosition").value("top"));
    }

    @Test
    void testPricingPageContentIncludesHeroBanners() throws Exception {
        PricingHeroBannerRequest req = new PricingHeroBannerRequest(
                "Studio Setup",
                "https://res.cloudinary.com/demo/image/upload/v1/designtouch/pricing/studio.png",
                "designtouch/pricing/studio",
                "Studio Main",
                "center",
                1,
                true
        );
        mockMvc.perform(post("/api/pricing-page/banners")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/pricing-page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heroBanners.length()").value(1))
                .andExpect(jsonPath("$.heroBanners[0].label").value("Studio Setup"))
                .andExpect(jsonPath("$.heroBannerImageUrl").value("https://res.cloudinary.com/demo/image/upload/v1/designtouch/pricing/studio.png"));
    }
}

