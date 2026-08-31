package com.designtouch.backend.security;

import com.designtouch.backend.repository.AdminUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Runs once per request. Reads the "Authorization: Bearer <token>" header, and if
 * it's a valid JWT for a known admin user, marks the request as authenticated so
 * SecurityConfig's .authenticated() rule on /api/admin/** passes.
 *
 * Public GET endpoints (/api/team, /api/blogs, etc.) never hit this logic in a way
 * that blocks them — no token just means the request stays unauthenticated, which
 * is fine for routes that don't require it.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AdminUserRepository adminUserRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtService.isValid(token)) {
                String username = jwtService.extractUsername(token);

                adminUserRepository.findByUsername(username).ifPresent(admin -> {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            admin.getUsername(), null,
                            List.of(() -> "ROLE_" + admin.getRole().name()));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                });
            }
        }

        filterChain.doFilter(request, response);
    }
}
