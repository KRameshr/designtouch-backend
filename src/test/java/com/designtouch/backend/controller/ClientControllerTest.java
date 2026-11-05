package com.designtouch.backend.controller;

import com.designtouch.backend.dto.ClientRequest;
import com.designtouch.backend.entity.Client;
import com.designtouch.backend.exception.GlobalExceptionHandler;
import com.designtouch.backend.repository.ClientRepository;
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

class ClientControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<UUID, Client> database = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() {
        database.clear();

        ClientRepository repo = (ClientRepository) Proxy.newProxyInstance(
                ClientRepository.class.getClassLoader(),
                new Class<?>[]{ClientRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        Client c = (Client) args[0];
                        if (c.getId() == null) {
                            c.setId(UUID.randomUUID());
                        }
                        database.put(c.getId(), c);
                        return c;
                    }
                    if ("findById".equals(name)) {
                        UUID id = (UUID) args[0];
                        return Optional.ofNullable(database.get(id));
                    }
                    if ("findAllByOrderByDisplayOrderAsc".equals(name)) {
                        return database.values().stream()
                                .sorted(Comparator.comparingInt(c -> c.getDisplayOrder() != null ? c.getDisplayOrder() : 0))
                                .toList();
                    }
                    if ("findByIsActiveTrueAndIsFeaturedTrueOrderByDisplayOrderAsc".equals(name)) {
                        return database.values().stream()
                                .filter(c -> Boolean.TRUE.equals(c.getIsActive()) && Boolean.TRUE.equals(c.getIsFeatured()))
                                .sorted(Comparator.comparingInt(c -> c.getDisplayOrder() != null ? c.getDisplayOrder() : 0))
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

        ClientController controller = new ClientController(repo);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("1. Public GET /api/clients returns only active and featured clients ordered by displayOrder")
    void publicGetAllReturnsActiveAndFeaturedOnly() throws Exception {
        Client c1 = Client.builder().id(UUID.randomUUID()).clientName("Featured & Active").logoUrl("https://res.cloudinary.com/logo1.png").displayOrder(1).isActive(true).isFeatured(true).build();
        Client c2 = Client.builder().id(UUID.randomUUID()).clientName("Active but NOT Featured").logoUrl("https://res.cloudinary.com/logo2.png").displayOrder(2).isActive(true).isFeatured(false).build();
        Client c3 = Client.builder().id(UUID.randomUUID()).clientName("Featured but Inactive").logoUrl("https://res.cloudinary.com/logo3.png").displayOrder(3).isActive(false).isFeatured(true).build();

        database.put(c1.getId(), c1);
        database.put(c2.getId(), c2);
        database.put(c3.getId(), c3);

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].clientName", is("Featured & Active")))
                .andExpect(jsonPath("$[0].isFeatured", is(true)))
                .andExpect(jsonPath("$[0].isActive", is(true)));
    }

    @Test
    @DisplayName("2. Admin GET /api/clients/admin/all returns all clients including inactive and non-featured")
    void adminGetAllReturnsAll() throws Exception {
        Client c1 = Client.builder().id(UUID.randomUUID()).clientName("Client A").logoUrl("https://res.cloudinary.com/logo1.png").displayOrder(2).isActive(true).isFeatured(true).build();
        Client c2 = Client.builder().id(UUID.randomUUID()).clientName("Client B").logoUrl("https://res.cloudinary.com/logo2.png").displayOrder(1).isActive(false).isFeatured(false).build();

        database.put(c1.getId(), c1);
        database.put(c2.getId(), c2);

        mockMvc.perform(get("/api/clients/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].clientName", is("Client B")))
                .andExpect(jsonPath("$[1].clientName", is("Client A")));
    }

    @Test
    @DisplayName("3. POST /api/clients creates client with logo and defaults active/featured to true")
    void createClientSuccess() throws Exception {
        ClientRequest req = new ClientRequest(
                "Acme Corp",
                "https://res.cloudinary.com/acme.svg",
                "https://acme.com",
                5,
                true,
                true
        );

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientName", is("Acme Corp")))
                .andExpect(jsonPath("$.logoUrl", is("https://res.cloudinary.com/acme.svg")))
                .andExpect(jsonPath("$.websiteUrl", is("https://acme.com")))
                .andExpect(jsonPath("$.displayOrder", is(5)))
                .andExpect(jsonPath("$.isActive", is(true)))
                .andExpect(jsonPath("$.isFeatured", is(true)));

        assertThat(database).hasSize(1);
    }

    @Test
    @DisplayName("4. PUT /api/clients/{id} updates client and preserves existing logo if replacement is omitted")
    void updateClientPreservesLogoWhenOmitted() throws Exception {
        UUID id = UUID.randomUUID();
        Client existing = Client.builder()
                .id(id)
                .clientName("Old Name")
                .logoUrl("https://res.cloudinary.com/existing-logo.png")
                .websiteUrl("https://old.com")
                .displayOrder(1)
                .isActive(true)
                .isFeatured(true)
                .build();
        database.put(id, existing);

        ClientRequest req = new ClientRequest(
                "Updated Name",
                "", // empty logoUrl - must NOT overwrite existing logo
                "https://updated.com",
                2,
                true,
                false
        );

        mockMvc.perform(put("/api/clients/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientName", is("Updated Name")))
                .andExpect(jsonPath("$.logoUrl", is("https://res.cloudinary.com/existing-logo.png")))
                .andExpect(jsonPath("$.isFeatured", is(false)));

        assertThat(database.get(id).getLogoUrl()).isEqualTo("https://res.cloudinary.com/existing-logo.png");
    }

    @Test
    @DisplayName("5. PATCH /api/clients/{id}/toggle-featured toggles isFeatured status")
    void toggleFeatured() throws Exception {
        UUID id = UUID.randomUUID();
        Client client = Client.builder().id(id).clientName("Toggle Client").logoUrl("https://logo.png").isActive(true).isFeatured(true).build();
        database.put(id, client);

        mockMvc.perform(patch("/api/clients/" + id + "/toggle-featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFeatured", is(false)));

        assertThat(database.get(id).getIsFeatured()).isFalse();

        mockMvc.perform(patch("/api/clients/" + id + "/toggle-featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFeatured", is(true)));

        assertThat(database.get(id).getIsFeatured()).isTrue();
    }

    @Test
    @DisplayName("6. PATCH /api/clients/{id}/toggle-active toggles isActive status")
    void toggleActive() throws Exception {
        UUID id = UUID.randomUUID();
        Client client = Client.builder().id(id).clientName("Active Toggle").logoUrl("https://logo.png").isActive(true).isFeatured(true).build();
        database.put(id, client);

        mockMvc.perform(patch("/api/clients/" + id + "/toggle-active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive", is(false)));

        assertThat(database.get(id).getIsActive()).isFalse();
    }
}

