package com.designtouch.backend.repository;

import com.designtouch.backend.entity.ServicesPageContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServicesPageContentRepository extends JpaRepository<ServicesPageContent, Short> {

    default Optional<ServicesPageContent> findSingleton() {
        return findById((short) 1);
    }
}

