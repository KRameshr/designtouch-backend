package com.designtouch.backend.controller;

import com.designtouch.backend.dto.AboutContentRequest;
import com.designtouch.backend.entity.AboutContent;
import com.designtouch.backend.exception.GlobalExceptionHandler;
import com.designtouch.backend.repository.AboutContentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AboutContentControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicReference<AboutContent> storage = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        AboutContent initial = AboutContent.builder()
                .id((short) 1)
                .heading("WE ARE DESIGNTOUCH+")
                .subheading("Passionate storytellers and editors")
                .ctaText("Let's Create Together")
                .ctaLink("/contact")
                .heroBannerImageUrl("https://res.cloudinary.com/demo/image/upload/about_banner.jpg")
                .heroBannerImagePublicId("about_banner")
                .storyHeading("Turning Ideas Into Impact.")
                .storyParagraph1("Founded with a passion for creative storytelling.")
                .storyParagraph2("Delivering commercial visual impact.")
                .storyImageUrl("https://res.cloudinary.com/demo/image/upload/story.jpg")
                .founderName("Rohit Sharma")
                .founderDesignation("Founder, DesignTouch+")
                .build();
        storage.set(initial);

        AboutContentRepository repo = (AboutContentRepository) Proxy.newProxyInstance(
                AboutContentRepository.class.getClassLoader(),
                new Class<?>[]{AboutContentRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findById".equals(name)) {
                        return Optional.ofNullable(storage.get());
                    }
                    if ("save".equals(name)) {
                        AboutContent c = (AboutContent) args[0];
                        storage.set(c);
                        return c;
                    }
                    return null;
                }
        );

        AboutContentController controller = new AboutContentController(repo);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/about returns current about content")
    void testGetAboutContent() throws Exception {
        mockMvc.perform(get("/api/about"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heading", is("WE ARE DESIGNTOUCH+")))
                .andExpect(jsonPath("$.ctaText", is("Let's Create Together")))
                .andExpect(jsonPath("$.heroBannerImageUrl", is("https://res.cloudinary.com/demo/image/upload/about_banner.jpg")))
                .andExpect(jsonPath("$.storyHeading", is("Turning Ideas Into Impact.")))
                .andExpect(jsonPath("$.founderName", is("Rohit Sharma")))
                .andExpect(jsonPath("$.founderDesignation", is("Founder, DesignTouch+")));
    }

    @Test
    @DisplayName("PUT /api/about updates hero, banner, and story content")
    void testUpdateAboutContent() throws Exception {
        AboutContentRequest req = new AboutContentRequest(
                "NEW ABOUT HEADING",
                "New description text",
                "Start Your Project",
                "/contact#quote",
                "https://res.cloudinary.com/demo/image/upload/new_banner.jpg",
                "new_banner",
                "Our Journey & Vision",
                "Story paragraph one updated.",
                "Story paragraph two updated.",
                "https://res.cloudinary.com/demo/image/upload/new_story.jpg",
                "Rohit S.",
                "Managing Director & Founder",
                null,
                "https://res.cloudinary.com/demo/video/upload/story.mp4",
                null
        );

        mockMvc.perform(put("/api/about")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heading", is("NEW ABOUT HEADING")))
                .andExpect(jsonPath("$.ctaText", is("Start Your Project")))
                .andExpect(jsonPath("$.ctaLink", is("/contact#quote")))
                .andExpect(jsonPath("$.heroBannerImageUrl", is("https://res.cloudinary.com/demo/image/upload/new_banner.jpg")))
                .andExpect(jsonPath("$.storyHeading", is("Our Journey & Vision")))
                .andExpect(jsonPath("$.founderName", is("Rohit S.")))
                .andExpect(jsonPath("$.founderDesignation", is("Managing Director & Founder")))
                .andExpect(jsonPath("$.storyVideoUrl", is("https://res.cloudinary.com/demo/video/upload/story.mp4")));

        AboutContent updated = storage.get();
        assertThat(updated.getHeading()).isEqualTo("NEW ABOUT HEADING");
        assertThat(updated.getHeroBannerImageUrl()).isEqualTo("https://res.cloudinary.com/demo/image/upload/new_banner.jpg");
        assertThat(updated.getFounderDesignation()).isEqualTo("Managing Director & Founder");
        assertThat(updated.getStoryVideoUrl()).isEqualTo("https://res.cloudinary.com/demo/video/upload/story.mp4");
    }

    @Test
    @DisplayName("DELETE /api/about/hero-banner clears hero banner without altering story text")
    void testDeleteHeroBanner() throws Exception {
        mockMvc.perform(delete("/api/about/hero-banner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heading", is("WE ARE DESIGNTOUCH+")))
                .andExpect(jsonPath("$.heroBannerImageUrl").isEmpty())
                .andExpect(jsonPath("$.storyHeading", is("Turning Ideas Into Impact.")));

        AboutContent updated = storage.get();
        assertThat(updated.getHeroBannerImageUrl()).isNull();
        assertThat(updated.getHeading()).isEqualTo("WE ARE DESIGNTOUCH+");
        assertThat(updated.getStoryHeading()).isEqualTo("Turning Ideas Into Impact.");
    }
}

