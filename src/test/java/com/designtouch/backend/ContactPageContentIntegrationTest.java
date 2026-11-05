package com.designtouch.backend;

import com.designtouch.backend.dto.ContactPageContentRequest;
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
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class ContactPageContentIntegrationTest {

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
        AdminUser admin = adminUserRepository.findByUsername("admin")
                .orElseGet(() -> adminUserRepository.save(AdminUser.builder()
                        .username("admin")
                        .passwordHash("$2a$10$dummyHashForTestingPurposesOnly")
                        .build()));
        adminToken = jwtService.generateToken(admin.getUsername());
    }

    @Test
    void testGetContactPageContent_public_success() throws Exception {
        mockMvc.perform(get("/api/contact-page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heroEyebrow").value("Contact Us"))
                .andExpect(jsonPath("$.heroHeadlineLine1").value("Let's Create Something"))
                .andExpect(jsonPath("$.heroHeadlineHighlight").value("Amazing."))
                .andExpect(jsonPath("$.heroDescription").isNotEmpty())
                .andExpect(jsonPath("$.badge1Title").value("Fast Response"))
                .andExpect(jsonPath("$.badge1Subtitle").value("We reply within 24 hours"))
                .andExpect(jsonPath("$.badge2Title").value("Project Focused"))
                .andExpect(jsonPath("$.badge2Subtitle").value("Solutions tailored for you"))
                .andExpect(jsonPath("$.directContactHeading").value("DIRECT CONTACT"))
                .andExpect(jsonPath("$.directContactEmail").value("hello@designtouchplus.com"))
                .andExpect(jsonPath("$.directContactPhone").value("+91 12345 67890"))
                .andExpect(jsonPath("$.directContactLocation").value("India · Remote Worldwide"))
                .andExpect(jsonPath("$.directContactWorkingHours").value("Monday - Saturday, 10:00 AM - 7:00 PM (IST)"))
                .andExpect(jsonPath("$.directContactEmailEnabled").value(true))
                .andExpect(jsonPath("$.directContactPhoneEnabled").value(true))
                .andExpect(jsonPath("$.directContactLocationEnabled").value(true))
                .andExpect(jsonPath("$.directContactWorkingHoursEnabled").value(true))
                .andExpect(jsonPath("$.socialLinks").isNotEmpty());
    }

    @Test
    void testUpdateContactPageContent_unauthenticated_forbidden() throws Exception {
        ContactPageContentRequest request = new ContactPageContentRequest(
                "UPDATED EYEBROW", "LET'S TALK", "NOW.", "Updated description",
                "Speedy", "12h reply", "Dedicated", "Custom fit",
                null, null, "Alt text", "center",
                "GET IN TOUCH", "DIRECT CONTACT", "Reach out anytime",
                "EMAIL US", "hello@designtouchplus.com", "We reply in 24h",
                "CALL / WHATSAPP", "+91 12345 67890", "Mon - Sat: 10AM - 7PM",
                "OUR LOCATION", "India · Remote Worldwide", "Global clients",
                "WORKING HOURS", "Mon - Sat, 10am - 7pm", ""
        );

        mockMvc.perform(put("/api/contact-page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateContactPageContent_authenticated_success() throws Exception {
        ContactPageContentRequest request = new ContactPageContentRequest(
                "GET IN TOUCH",
                "START YOUR PROJECT",
                "TODAY.",
                "Tell us what you want to build and we will make it happen.",
                "Instant Reply",
                "Under 2 hours",
                "Bespoke Editing",
                "Handcrafted storytelling",
                "https://res.cloudinary.com/test-cloud/image/upload/v12345/designtouch/contact/workstation.jpg",
                "designtouch/contact/workstation",
                "DesignTouch Studio Workstation",
                "center",
                "GET IN TOUCH NOW",
                "DIRECT REACH",
                "Contact our core creative team directly.",
                "WRITE US",
                "contact@designtouchplus.com",
                "Replies within 12 hours",
                "PHONE & WHATSAPP",
                "+91 98765 43210",
                "Available 9 AM - 8 PM IST",
                "STUDIO LOCATION",
                "Bangalore, India · Global Remote",
                "Collaborating with worldwide brands",
                "STUDIO HOURS",
                "Monday - Saturday, 9:00 AM - 8:00 PM (IST)",
                "Sunday: On-call emergency support"
        );

        mockMvc.perform(put("/api/contact-page")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heroEyebrow").value("GET IN TOUCH"))
                .andExpect(jsonPath("$.heroHeadlineLine1").value("START YOUR PROJECT"))
                .andExpect(jsonPath("$.heroHeadlineHighlight").value("TODAY."))
                .andExpect(jsonPath("$.heroDescription").value("Tell us what you want to build and we will make it happen."))
                .andExpect(jsonPath("$.badge1Title").value("Instant Reply"))
                .andExpect(jsonPath("$.badge1Subtitle").value("Under 2 hours"))
                .andExpect(jsonPath("$.badge2Title").value("Bespoke Editing"))
                .andExpect(jsonPath("$.badge2Subtitle").value("Handcrafted storytelling"))
                .andExpect(jsonPath("$.heroBannerImageUrl").value("https://res.cloudinary.com/test-cloud/image/upload/v12345/designtouch/contact/workstation.jpg"))
                .andExpect(jsonPath("$.heroBannerImagePublicId").value("designtouch/contact/workstation"))
                .andExpect(jsonPath("$.directContactHeading").value("DIRECT REACH"))
                .andExpect(jsonPath("$.directContactEmail").value("contact@designtouchplus.com"))
                .andExpect(jsonPath("$.directContactPhone").value("+91 98765 43210"))
                .andExpect(jsonPath("$.directContactLocation").value("Bangalore, India · Global Remote"))
                .andExpect(jsonPath("$.directContactWorkingHours").value("Monday - Saturday, 9:00 AM - 8:00 PM (IST)"));

        // Public GET also returns the newly updated content
        mockMvc.perform(get("/api/contact-page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heroEyebrow").value("GET IN TOUCH"))
                .andExpect(jsonPath("$.badge1Title").value("Instant Reply"))
                .andExpect(jsonPath("$.directContactEmail").value("contact@designtouchplus.com"))
                .andExpect(jsonPath("$.directContactPhone").value("+91 98765 43210"));
    }

    @Test
    void testUpdateContactPageContent_validationOversizedField_badRequest() throws Exception {
        String oversizedEyebrow = "A".repeat(151);
        ContactPageContentRequest request = new ContactPageContentRequest(
                oversizedEyebrow, "Heading", "Highlight", "Description",
                "Badge 1", "Sub 1", "Badge 2", "Sub 2",
                null, null, "Alt", "center",
                "EYEBROW", "HEADING", "DESC",
                "EMAIL US", "hello@test.com", "Sub",
                "PHONE", "+123", "Sub",
                "LOCATION", "Loc", "Sub",
                "HOURS", "Hours", "Sub"
        );

        mockMvc.perform(put("/api/contact-page")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteHeroBannerImage_authenticated_success() throws Exception {
        // First set an image
        ContactPageContentRequest request = new ContactPageContentRequest(
                "Contact Us", "Let's Create", "Something.", "Desc",
                "Fast", "24h", "Project", "Tailored",
                "https://res.cloudinary.com/test-cloud/image/upload/v12345/designtouch/contact/workstation.jpg",
                "designtouch/contact/workstation",
                "Alt", "center",
                "GET IN TOUCH", "DIRECT CONTACT", "Desc",
                "EMAIL US", "hello@designtouchplus.com", "Sub",
                "CALL / WHATSAPP", "+91 12345 67890", "Sub",
                "OUR LOCATION", "India", "Sub",
                "WORKING HOURS", "Mon-Sat", ""
        );

        mockMvc.perform(put("/api/contact-page")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heroBannerImageUrl").isNotEmpty());

        // Then delete the hero banner image
        mockMvc.perform(delete("/api/contact-page/hero-banner")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heroBannerImageUrl").isEmpty())
                .andExpect(jsonPath("$.heroBannerImagePublicId").isEmpty());
    }
}

