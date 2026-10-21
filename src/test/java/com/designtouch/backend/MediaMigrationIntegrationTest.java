package com.designtouch.backend;

import com.cloudinary.Cloudinary;
import com.designtouch.backend.entity.AdminUser;
import com.designtouch.backend.repository.AdminUserRepository;
import com.designtouch.backend.security.JwtService;
import com.designtouch.backend.service.MediaMigrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class MediaMigrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private MediaMigrationService migrationService;

    @MockBean
    private Cloudinary cloudinary;

    private String adminToken;

    @BeforeEach
    void setUp() {
        AdminUser admin = adminUserRepository.findAll().stream().findFirst().orElseThrow();
        adminToken = jwtService.generateToken(admin.getUsername());
    }

    @Test
    void testGetInventory_unauthenticated_forbidden() throws Exception {
        mockMvc.perform(get("/api/media/migration/inventory"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetInventory_authenticated_returnsReport() throws Exception {
        mockMvc.perform(get("/api/media/migration/inventory")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(greaterThanOrEqualTo(10)))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(10))));
    }

    @Test
    void testRunMigration_dryRun_returnsReportWithoutMutations() throws Exception {
        mockMvc.perform(post("/api/media/migration/run?dryRun=true")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dryRun").value(true))
                .andExpect(jsonPath("$.totalProjects").value(greaterThanOrEqualTo(10)))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void testResolveLocalFile_findsHeroDemoMp4() {
        File file = migrationService.resolveLocalFile("/hero-demo.mp4");
        assertNotNull(file, "Expected hero-demo.mp4 to be resolved from local directory");
    }
}

