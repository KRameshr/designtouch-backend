package com.designtouch.backend;

import com.designtouch.backend.dto.FaqRequest;
import com.designtouch.backend.entity.AdminUser;
import com.designtouch.backend.entity.Faq;
import com.designtouch.backend.entity.enums.FaqCategory;
import com.designtouch.backend.repository.AdminUserRepository;
import com.designtouch.backend.repository.FaqRepository;
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

import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class FaqControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FaqRepository faqRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private JwtService jwtService;

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
    }

    @Test
    void testGetFaqs_public_returnsActiveFaqsByCategory() throws Exception {
        // Ensure at least one active FAQ exists for GENERAL
        faqRepository.save(Faq.builder()
                .question("How do we get started?")
                .answer("Contact us through our form or WhatsApp to schedule a kickoff.")
                .category(FaqCategory.GENERAL)
                .displayOrder(1)
                .isActive(true)
                .build());

        mockMvc.perform(get("/api/faqs")
                        .param("category", "GENERAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].question").exists())
                .andExpect(jsonPath("$[0].answer").exists())
                .andExpect(jsonPath("$[0].category").value("GENERAL"));
    }

    @Test
    void testCreateFaq_withoutAuth_failsUnauthorizedOrForbidden() throws Exception {
        FaqRequest request = new FaqRequest(
                "Unauthorized question?",
                "Unauthorized answer.",
                FaqCategory.GENERAL,
                1,
                true
        );

        mockMvc.perform(post("/api/faqs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateFaq_withAuth_success() throws Exception {
        FaqRequest request = new FaqRequest(
                "What is your turnaround time?",
                "Turnaround varies by scope, but standard edits take 24-48 hours.",
                FaqCategory.GENERAL,
                1,
                true
        );

        mockMvc.perform(post("/api/faqs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.question").value(request.question()))
                .andExpect(jsonPath("$.answer").value(request.answer()))
                .andExpect(jsonPath("$.category").value("GENERAL"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void testCreateFaq_inputValidation_blankFields_returnsBadRequest() throws Exception {
        FaqRequest invalidRequest = new FaqRequest(
                "", // Blank question
                "Valid answer.",
                FaqCategory.GENERAL,
                1,
                true
        );

        mockMvc.perform(post("/api/faqs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateFaq_withAuth_success() throws Exception {
        Faq saved = faqRepository.save(Faq.builder()
                .question("Original question?")
                .answer("Original answer.")
                .category(FaqCategory.GENERAL)
                .displayOrder(1)
                .isActive(true)
                .build());

        FaqRequest updateRequest = new FaqRequest(
                "Updated question?",
                "Updated answer text with more details.",
                FaqCategory.GENERAL,
                2,
                true
        );

        mockMvc.perform(put("/api/faqs/" + saved.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.question").value("Updated question?"))
                .andExpect(jsonPath("$.answer").value("Updated answer text with more details."));
    }

    @Test
    void testDeleteFaq_withAuth_success() throws Exception {
        Faq saved = faqRepository.save(Faq.builder()
                .question("Temporary question to delete?")
                .answer("Temporary answer.")
                .category(FaqCategory.GENERAL)
                .displayOrder(1)
                .isActive(true)
                .build());

        UUID id = saved.getId();

        mockMvc.perform(delete("/api/faqs/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertFalse(faqRepository.existsById(id));
    }

    @Test
    void testGetFaqsAdmin_returnsAllIncludingInactive() throws Exception {
        faqRepository.save(Faq.builder()
                .question("Inactive question?")
                .answer("Answer.")
                .category(FaqCategory.GENERAL)
                .displayOrder(99)
                .isActive(false)
                .build());

        mockMvc.perform(get("/api/faqs/admin/all")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("category", "GENERAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }
}

