package com.designtouch.backend.config;

import com.designtouch.backend.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            // Stateless REST API — no CSRF tokens or server-side sessions needed,
            // the JWT itself is the auth mechanism on every request.
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Preflight OPTIONS requests
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                // Public reads + auth endpoints + first-time admin bootstrap
                .requestMatchers("/api/auth/**").permitAll()
                // Admin-only endpoints that must be protected even for GET requests
                .requestMatchers(
                    "/api/portfolio/admin/**",
                    "/api/blogs/admin/**",
                    "/api/blog-categories/admin/**",
                    "/api/media/migration/**",
                    "/api/services/admin/**",
                    "/api/process-steps/admin/**",
                    "/api/tools/admin/**",
                    "/api/services-page/banners/admin/**",
                    "/api/pricing-page/banners/admin/**",
                    "/api/pricing-plans/admin/**",
                    "/api/addons/admin/**",
                    "/api/faqs/admin/**"
                ).authenticated()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/contact", "/api/newsletter/**").permitAll()
                // Everything else under /api/** (writes to portfolio, blog, pricing, etc.)
                // requires a valid JWT for an admin user
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
