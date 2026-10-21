package com.designtouch.backend;

import com.designtouch.backend.dto.StatItemRequest;
import com.designtouch.backend.entity.AdminUser;
import com.designtouch.backend.repository.AdminUserRepository;
import com.designtouch.backend.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class StatItemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() {
        AdminUser admin = adminUserRepository.findAll().stream().findFirst().orElseThrow();
        adminToken = jwtService.generateToken(admin.getUsername());
    }

    @Test
    void testGetStats_public_returnsStatsList() throws Exception {
        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testCreateStat_unauthenticated_forbidden() throws Exception {
        StatItemRequest request = new StatItemRequest(
                "Award Wins", "25+", "+", "Global creative awards", null, 5
        );

        mockMvc.perform(post("/api/stats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testStatLifecycle_admin_createUpdateDelete() throws Exception {
        // 1. Create stat
        StatItemRequest createRequest = new StatItemRequest(
                "Client Retention", "98%", "%", "Industry leading retention", null, 10
        );

        String createResponse = mockMvc.perform(post("/api/stats")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("Client Retention"))
                .andExpect(jsonPath("$.value").value("98%"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String statId = objectMapper.readTree(createResponse).get("id").asText();

        // 2. Update stat (e.g. 98% -> 99%)
        StatItemRequest updateRequest = new StatItemRequest(
                "Client Retention", "99%", "%", "Updated retention rate", null, 10
        );

        mockMvc.perform(put("/api/stats/" + statId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("99%"))
                .andExpect(jsonPath("$.description").value("Updated retention rate"));

        // 3. Delete stat
        mockMvc.perform(delete("/api/stats/" + statId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}

