package com.designtouch.backend.repository;

import com.designtouch.backend.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<Service, UUID> {

    List<Service> findAllByOrderByDisplayOrderAsc();

    // "LEARN MORE" -> /services/{slug} detail page
    Optional<Service> findBySlug(String slug);
}
