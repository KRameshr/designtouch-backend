package com.designtouch.backend.controller;

import com.designtouch.backend.dto.LoginRequest;
import com.designtouch.backend.dto.LoginResponse;
import com.designtouch.backend.entity.AdminUser;
import com.designtouch.backend.entity.enums.AdminRole;
import com.designtouch.backend.repository.AdminUserRepository;
import com.designtouch.backend.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AdminUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AdminUser admin = repository.findByUsername(request.username())
                .filter(a -> passwordEncoder.matches(request.password(), a.getPasswordHash()))
                .orElse(null);

        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwtService.generateToken(admin.getUsername());
        return ResponseEntity.ok(new LoginResponse(token, admin.getUsername(), admin.getRole().name()));
    }

    // One-time bootstrap: only works while the admin_user table is empty.
    // After the first admin exists, this always returns 403 — new admins are
    // added by an existing admin through a (future) authenticated endpoint,
    // never through this public one.
    @PostMapping("/register-first-admin")
    public ResponseEntity<Void> registerFirstAdmin(@Valid @RequestBody LoginRequest request) {
        if (repository.count() > 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        AdminUser admin = AdminUser.builder()
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(AdminRole.SUPER_ADMIN)
                .build();
        repository.save(admin);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
