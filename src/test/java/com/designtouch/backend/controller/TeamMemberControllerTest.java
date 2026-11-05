package com.designtouch.backend.controller;

import com.designtouch.backend.dto.TeamMemberRequest;
import com.designtouch.backend.entity.TeamMember;
import com.designtouch.backend.exception.GlobalExceptionHandler;
import com.designtouch.backend.repository.TeamMemberRepository;
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

class TeamMemberControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<UUID, TeamMember> database = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() {
        database.clear();

        TeamMemberRepository repo = (TeamMemberRepository) Proxy.newProxyInstance(
                TeamMemberRepository.class.getClassLoader(),
                new Class<?>[]{TeamMemberRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        TeamMember m = (TeamMember) args[0];
                        if (m.getId() == null) {
                            m.setId(UUID.randomUUID());
                        }
                        database.put(m.getId(), m);
                        return m;
                    }
                    if ("findById".equals(name)) {
                        UUID id = (UUID) args[0];
                        return Optional.ofNullable(database.get(id));
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
                    if ("findAll".equals(name)) {
                        return new ArrayList<>(database.values());
                    }
                    if ("findByIsActiveTrueOrderByDisplayOrderAsc".equals(name)) {
                        List<TeamMember> list = new ArrayList<>();
                        for (TeamMember m : database.values()) {
                            if (Boolean.TRUE.equals(m.getIsActive())) {
                                list.add(m);
                            }
                        }
                        list.sort(Comparator.comparing(TeamMember::getDisplayOrder, Comparator.nullsLast(Integer::compareTo)));
                        return list;
                    }
                    return null;
                }
        );

        TeamMemberController controller = new TeamMemberController(repo);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/team should return active team members with linkedinUrl and instagramUrl")
    void testGetActiveTeamMembers() throws Exception {
        UUID id = UUID.randomUUID();
        TeamMember member = TeamMember.builder()
                .id(id)
                .name("Rohit Sharma")
                .role("Founder & Creative Director")
                .photoUrl("https://example.com/rohit.jpg")
                .linkedinUrl("https://linkedin.com/in/rohit-sharma")
                .instagramUrl("https://instagram.com/rohitsharma")
                .displayOrder(1)
                .isActive(true)
                .build();
        database.put(id, member);

        mockMvc.perform(get("/api/team"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Rohit Sharma")))
                .andExpect(jsonPath("$[0].role", is("Founder & Creative Director")))
                .andExpect(jsonPath("$[0].linkedinUrl", is("https://linkedin.com/in/rohit-sharma")))
                .andExpect(jsonPath("$[0].instagramUrl", is("https://instagram.com/rohitsharma")));
    }

    @Test
    @DisplayName("POST /api/team should save new member with linkedinUrl and instagramUrl")
    void testCreateTeamMemberWithSocials() throws Exception {
        TeamMemberRequest request = new TeamMemberRequest(
                "Arjun Verma",
                "Head of Video Production",
                "https://example.com/arjun.jpg",
                "https://linkedin.com/in/arjun-verma",
                "https://instagram.com/arjunverma",
                2,
                true
        );

        mockMvc.perform(post("/api/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Arjun Verma")))
                .andExpect(jsonPath("$.role", is("Head of Video Production")))
                .andExpect(jsonPath("$.linkedinUrl", is("https://linkedin.com/in/arjun-verma")))
                .andExpect(jsonPath("$.instagramUrl", is("https://instagram.com/arjunverma")));

        assertThat(database).hasSize(1);
        TeamMember saved = database.values().iterator().next();
        assertThat(saved.getLinkedinUrl()).isEqualTo("https://linkedin.com/in/arjun-verma");
        assertThat(saved.getInstagramUrl()).isEqualTo("https://instagram.com/arjunverma");
    }

    @Test
    @DisplayName("PUT /api/team/{id} should update or clear linkedinUrl and instagramUrl independently")
    void testUpdateAndClearSocialUrls() throws Exception {
        UUID id = UUID.randomUUID();
        TeamMember existing = TeamMember.builder()
                .id(id)
                .name("Pooja Nair")
                .role("Motion Graphics Designer")
                .photoUrl("https://example.com/pooja.jpg")
                .linkedinUrl("https://linkedin.com/in/pooja-nair")
                .instagramUrl("https://instagram.com/poojanair")
                .displayOrder(3)
                .isActive(true)
                .build();
        database.put(id, existing);

        // Update: update LinkedIn, clear Instagram
        TeamMemberRequest updateReq = new TeamMemberRequest(
                "Pooja Nair",
                "Lead Motion Designer",
                "https://example.com/pooja.jpg",
                "https://linkedin.com/in/pooja-updated",
                "", // cleared
                3,
                true
        );

        mockMvc.perform(put("/api/team/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("Lead Motion Designer")))
                .andExpect(jsonPath("$.linkedinUrl", is("https://linkedin.com/in/pooja-updated")))
                .andExpect(jsonPath("$.instagramUrl", nullValue()));

        TeamMember updated = database.get(id);
        assertThat(updated.getLinkedinUrl()).isEqualTo("https://linkedin.com/in/pooja-updated");
        assertThat(updated.getInstagramUrl()).isNull();
    }
}

