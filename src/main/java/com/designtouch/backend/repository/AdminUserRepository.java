package com.designtouch.backend.repository;

import com.designtouch.backend.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminUserRepository extends JpaRepository<AdminUser, UUID> {

    // Used by the login endpoint to look up the user before verifying the password
    Optional<AdminUser> findByUsername(String username);
}
