package com.designtouch.backend.controller;

import com.designtouch.backend.dto.TestimonialRequest;
import com.designtouch.backend.entity.Testimonial;
import com.designtouch.backend.exception.GlobalExceptionHandler;
import com.designtouch.backend.repository.TestimonialRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TestimonialControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<UUID, Testimonial> database = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() {
        database.clear();

        // Standard JDK dynamic proxy to avoid ByteBuddy agent attachment delays
        TestimonialRepository repo = (TestimonialRepository) Proxy.newProxyInstance(
                TestimonialRepository.class.getClassLoader(),
                new Class<?>[]{TestimonialRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        Testimonial t = (Testimonial) args[0];
                        if (t.getId() == null) {
                            t.setId(UUID.randomUUID());
                        }
                        database.put(t.getId(), t);
                        return t;
                    }
                    if ("findById".equals(name)) {
                        UUID id = (UUID) args[0];
                        return Optional.ofNullable(database.get(id));
                    }
                    if ("findAll".equals(name)) {
                        return new ArrayList<>(database.values());
                    }
                    if ("findByIsActiveTrueOrderByDisplayOrderAsc".equals(name)) {
                        return database.values().stream()
                                .filter(t -> Boolean.TRUE.equals(t.getIsActive()))
                                .sorted(Comparator.comparingInt(t -> t.getDisplayOrder() != null ? t.getDisplayOrder() : 0))
                                .toList();
                    }
                    if ("existsById".equals(name)) {
                        UUID id = (UUID) args[0];
                        return database.containsKey(id);
                    }
                    if ("deleteById".equals(name)) {
                        UUID id = (UUID) args[0];
                        database.remove(id);
                        return null;
                    }
                    return null;
                }
        );

        TestimonialController controller = new TestimonialController(repo);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/testimonials returns active testimonials with authorAvatarUrl and serviceType")
    void getAll_returnsActiveTestimonialsWithAvatar() throws Exception {
        UUID id = UUID.randomUUID();
        Testimonial t1 = Testimonial.builder()
                .id(id)
                .quote("Fantastic work on our channel.")
                .authorName("Alex Morgan")
                .authorRole("Head of Content")
                .authorCompany("Acme Media")
                .authorAvatarUrl("https://res.cloudinary.com/designtouch/image/upload/v1/alex.jpg")
                .serviceType("Video Editing")
                .rating((short) 5)
                .displayOrder(1)
                .isActive(true)
                .build();
        database.put(id, t1);

        mockMvc.perform(get("/api/testimonials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].authorName").value("Alex Morgan"))
                .andExpect(jsonPath("$[0].authorRole").value("Head of Content"))
                .andExpect(jsonPath("$[0].authorCompany").value("Acme Media"))
                .andExpect(jsonPath("$[0].authorAvatarUrl").value("https://res.cloudinary.com/designtouch/image/upload/v1/alex.jpg"))
                .andExpect(jsonPath("$[0].serviceType").value("Video Editing"))
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    @Test
    @DisplayName("GET /api/testimonials gracefully returns 'General Feedback' fallback when serviceType is null")
    void getAll_handlesTestimonialWithoutAvatarGracefully() throws Exception {
        UUID id = UUID.randomUUID();
        Testimonial t = Testimonial.builder()
                .id(id)
                .quote("Great speed and quality.")
                .authorName("Sarah Jenkins")
                .authorRole("Producer")
                .authorCompany("Studio Nine")
                .authorAvatarUrl(null)
                .serviceType(null)
                .rating((short) 5)
                .displayOrder(0)
                .isActive(true)
                .build();
        database.put(id, t);

        mockMvc.perform(get("/api/testimonials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].authorName").value("Sarah Jenkins"))
                .andExpect(jsonPath("$[0].authorAvatarUrl").doesNotExist())
                .andExpect(jsonPath("$[0].serviceType").value("General Feedback"));
    }

    @Test
    @DisplayName("GET /api/testimonials/admin/all returns all testimonials including serviceType, displayOrder and isActive")
    void getAllForAdmin_returnsAll() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Testimonial t1 = Testimonial.builder()
                .id(id1)
                .quote("Active review")
                .authorName("Client One")
                .authorRole("CEO")
                .authorAvatarUrl("https://res.cloudinary.com/one.jpg")
                .serviceType("YouTube Thumbnail Design")
                .rating((short) 5)
                .displayOrder(0)
                .isActive(true)
                .build();

        Testimonial t2 = Testimonial.builder()
                .id(id2)
                .quote("Inactive review")
                .authorName("Client Two")
                .authorRole("CTO")
                .authorAvatarUrl(null)
                .serviceType(null)
                .rating((short) 4)
                .displayOrder(1)
                .isActive(false)
                .build();

        database.put(id1, t1);
        database.put(id2, t2);

        mockMvc.perform(get("/api/testimonials/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].authorName", containsInAnyOrder("Client One", "Client Two")))
                .andExpect(jsonPath("$[*].serviceType", containsInAnyOrder("YouTube Thumbnail Design", "General Feedback")));
    }

    @Test
    @DisplayName("POST /api/testimonials creates testimonial with serviceType and authorAvatarUrl")
    void create_createsTestimonialWithAvatar() throws Exception {
        TestimonialRequest req = new TestimonialRequest(
                "World-class editing and turnaround.",
                "Michael Chen",
                "Creator",
                "ChenVlogs",
                "https://res.cloudinary.com/designtouch/testimonials/michael.webp",
                (short) 5,
                2,
                true,
                "YouTube Thumbnail Design"
        );

        mockMvc.perform(post("/api/testimonials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.authorName").value("Michael Chen"))
                .andExpect(jsonPath("$.authorRole").value("Creator"))
                .andExpect(jsonPath("$.authorCompany").value("ChenVlogs"))
                .andExpect(jsonPath("$.authorAvatarUrl").value("https://res.cloudinary.com/designtouch/testimonials/michael.webp"))
                .andExpect(jsonPath("$.serviceType").value("YouTube Thumbnail Design"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.displayOrder").value(2))
                .andExpect(jsonPath("$.isActive").value(true));

        assertThat(database.values()).hasSize(1);
        Testimonial saved = database.values().iterator().next();
        assertThat(saved.getAuthorAvatarUrl()).isEqualTo("https://res.cloudinary.com/designtouch/testimonials/michael.webp");
        assertThat(saved.getServiceType()).isEqualTo("YouTube Thumbnail Design");
        assertThat(saved.getAuthorName()).isEqualTo("Michael Chen");
    }

    @Test
    @DisplayName("PUT /api/testimonials/{id} updates testimonial and replaces serviceType")
    void update_updatesTestimonialAndAvatarUrl() throws Exception {
        UUID id = UUID.randomUUID();
        Testimonial existing = Testimonial.builder()
                .id(id)
                .quote("Initial review")
                .authorName("David Miller")
                .authorRole("Marketer")
                .authorCompany("Growth Inc")
                .authorAvatarUrl("https://res.cloudinary.com/old-avatar.jpg")
                .serviceType("Video Editing")
                .rating((short) 4)
                .displayOrder(0)
                .isActive(true)
                .build();
        database.put(id, existing);

        TestimonialRequest updateReq = new TestimonialRequest(
                "Updated review: unbelievable conversion boost!",
                "David Miller",
                "VP Growth",
                "Growth Inc",
                "https://res.cloudinary.com/new-avatar.png",
                (short) 5,
                1,
                true,
                "Commercial Video Production"
        );

        mockMvc.perform(put("/api/testimonials/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.authorRole").value("VP Growth"))
                .andExpect(jsonPath("$.authorAvatarUrl").value("https://res.cloudinary.com/new-avatar.png"))
                .andExpect(jsonPath("$.serviceType").value("Commercial Video Production"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.displayOrder").value(1));

        Testimonial updated = database.get(id);
        assertThat(updated.getAuthorAvatarUrl()).isEqualTo("https://res.cloudinary.com/new-avatar.png");
        assertThat(updated.getServiceType()).isEqualTo("Commercial Video Production");
        assertThat(updated.getAuthorRole()).isEqualTo("VP Growth");
    }

    @Test
    @DisplayName("DELETE /api/testimonials/{id} deletes testimonial by id")
    void delete_removesTestimonial() throws Exception {
        UUID id = UUID.randomUUID();
        Testimonial t = Testimonial.builder()
                .id(id)
                .quote("To be deleted")
                .authorName("Temp Client")
                .build();
        database.put(id, t);

        mockMvc.perform(delete("/api/testimonials/" + id))
                .andExpect(status().isNoContent());

        assertThat(database).doesNotContainKey(id);
    }
}
