package com.designtouch.backend;

import com.designtouch.backend.entity.AdminUser;
import com.designtouch.backend.entity.enums.AdminRole;
import com.designtouch.backend.repository.AdminUserRepository;
import com.designtouch.backend.repository.ToolRepository;
import com.designtouch.backend.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ToolControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String getAdminToken() {
        AdminUser admin = adminUserRepository.findAll().stream().findFirst().orElseGet(() -> {
            AdminUser newAdmin = AdminUser.builder()
                    .username("test_admin")
                    .passwordHash("hash")
                    .role(AdminRole.SUPER_ADMIN)
                    .build();
            return adminUserRepository.save(newAdmin);
        });
        return jwtService.generateToken(admin.getUsername());
    }

    @Test
    void toolLifecycleWithLocalIconsAndFallbackWorksEndToEnd() throws Exception {
        String token = getAdminToken();
        UUID createdId = null;

        try {
            // 1. Create a tool with an existing local icon
            String createBody = """
                {
                    "name": "Cinema 4D Test",
                    "iconKey": "C4D",
                    "logoUrl": "/assets/tools/blender.svg",
                    "websiteUrl": "https://maxon.net",
                    "role": "3D Motion Design",
                    "displayOrder": 99,
                    "isActive": true
                }
                """;

            MvcResult createRes = mockMvc.perform(post("/api/tools")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Cinema 4D Test"))
                    .andExpect(jsonPath("$.iconKey").value("C4D"))
                    .andExpect(jsonPath("$.logoUrl").value("/assets/tools/blender.svg"))
                    .andReturn();

            JsonNode createdJson = objectMapper.readTree(createRes.getResponse().getContentAsString());
            createdId = UUID.fromString(createdJson.get("id").asText());

            // 2. Replace icon with a custom uploaded local SVG path and verify other fields are preserved
            String replaceIconBody = """
                {
                    "name": "Cinema 4D Test",
                    "iconKey": "C4D",
                    "logoUrl": "/assets/tools/cinema-4d-custom.svg",
                    "websiteUrl": "https://maxon.net",
                    "role": "3D Motion Design",
                    "displayOrder": 99,
                    "isActive": true
                }
                """;

            mockMvc.perform(put("/api/tools/" + createdId)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(replaceIconBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.logoUrl").value("/assets/tools/cinema-4d-custom.svg"))
                    .andExpect(jsonPath("$.iconKey").value("C4D"))
                    .andExpect(jsonPath("$.role").value("3D Motion Design"))
                    .andExpect(jsonPath("$.websiteUrl").value("https://maxon.net"));

            // 3. Remove icon assignment (empty logoUrl for monogram fallback)
            String removeIconBody = """
                {
                    "name": "Cinema 4D Test",
                    "iconKey": "C4D",
                    "logoUrl": "",
                    "websiteUrl": "https://maxon.net",
                    "role": "3D Motion Design",
                    "displayOrder": 99,
                    "isActive": true
                }
                """;

            mockMvc.perform(put("/api/tools/" + createdId)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(removeIconBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.logoUrl").value(""))
                    .andExpect(jsonPath("$.iconKey").value("C4D"));

            // 4. Reject base64 data URI in logoUrl
            String invalidBase64Body = """
                {
                    "name": "Cinema 4D Test",
                    "iconKey": "C4D",
                    "logoUrl": "data:image/svg+xml;base64,PHN2Zz48L3N2Zz4=",
                    "websiteUrl": "https://maxon.net",
                    "role": "3D Motion Design",
                    "displayOrder": 99,
                    "isActive": true
                }
                """;

            mockMvc.perform(put("/api/tools/" + createdId)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBase64Body))
                    .andExpect(status().isBadRequest());
        } finally {
            if (createdId != null && toolRepository.existsById(createdId)) {
                toolRepository.deleteById(createdId);
            }
        }
    }
}

