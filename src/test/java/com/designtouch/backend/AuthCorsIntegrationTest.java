package com.designtouch.backend;

import com.designtouch.backend.config.CorsConfig;
import com.designtouch.backend.config.SecurityConfig;
import com.designtouch.backend.controller.AuthController;
import com.designtouch.backend.repository.AdminUserRepository;
import com.designtouch.backend.security.JwtAuthFilter;
import com.designtouch.backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({CorsConfig.class, SecurityConfig.class, JwtAuthFilter.class})
// This slice intentionally excludes the application DataSource and Flyway.
class AuthCorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUserRepository adminUserRepository;

    @MockBean
    private JwtService jwtService;

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
}

