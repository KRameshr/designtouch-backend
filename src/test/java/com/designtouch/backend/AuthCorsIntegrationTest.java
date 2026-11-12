package com.designtouch.backend;

import com.designtouch.backend.config.CorsConfig;
import com.designtouch.backend.config.SecurityConfig;
import com.designtouch.backend.controller.AuthController;
import com.designtouch.backend.controller.ClientController;
import com.designtouch.backend.controller.TeamMemberController;
import com.designtouch.backend.controller.TestimonialController;
import com.designtouch.backend.entity.AdminUser;
import com.designtouch.backend.entity.enums.AdminRole;
import com.designtouch.backend.repository.AdminUserRepository;
import com.designtouch.backend.repository.ClientRepository;
import com.designtouch.backend.repository.TeamMemberRepository;
import com.designtouch.backend.repository.TestimonialRepository;
import com.designtouch.backend.security.JwtAuthFilter;
import com.designtouch.backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, TeamMemberController.class, ClientController.class, TestimonialController.class})
@Import({CorsConfig.class, SecurityConfig.class, JwtAuthFilter.class, JwtService.class})
// This slice intentionally excludes the application DataSource and Flyway.
class AuthCorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration;

    @MockBean
    private AdminUserRepository adminUserRepository;

    @MockBean
    private TeamMemberRepository teamMemberRepository;

    @MockBean
    private ClientRepository clientRepository;

    @MockBean
    private TestimonialRepository testimonialRepository;

    @Test
    void jwtFilterIsNotSeparatelyRegisteredWithTheServletContainer() {
        org.assertj.core.api.Assertions.assertThat(jwtAuthFilterRegistration.isEnabled()).isFalse();
    }

    @Test
    void preflightLoginFromLocalhost3001IsAllowed() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:3001")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type, Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3001"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("Content-Type")));
    }

    @Test
    void preflightLoginFromLocalhost3000IsAllowed() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")));
    }

    @Test
    void preflightLoginFromUnallowedOriginIsRejected() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "https://unauthorized.example.com")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void postLoginFromLocalhost3001ReachesAuthControllerWithCorsHeader() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header("Origin", "http://localhost:3001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nonexistent_admin\",\"password\":\"wrong_password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3001"));
    }

    @Test
    void adminCollectionReadsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/team/admin/all")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/clients/admin/all")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/testimonials/admin/all")).andExpect(status().isForbidden());
    }

    @Test
    void authenticatedAdminCanReadAllThreeAdminCollectionsThroughJwtFilter() throws Exception {
        String username = "test-admin";
        AdminUser admin = AdminUser.builder()
                .username(username)
                .passwordHash("unused-in-this-test")
                .role(AdminRole.SUPER_ADMIN)
                .build();
        when(adminUserRepository.findByUsername(username)).thenReturn(Optional.of(admin));
        when(teamMemberRepository.findAll()).thenReturn(List.of());
        when(clientRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(List.of());
        when(testimonialRepository.findAll()).thenReturn(List.of());
        String token = jwtService.generateToken(username);

        mockMvc.perform(get("/api/team/admin/all").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/clients/admin/all").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/testimonials/admin/all").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        verify(adminUserRepository, times(3)).findByUsername(username);
        verify(teamMemberRepository).findAll();
        verify(clientRepository).findAllByOrderByDisplayOrderAsc();
        verify(testimonialRepository).findAll();
    }
}

