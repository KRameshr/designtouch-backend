package com.designtouch.backend.repository;

import com.designtouch.backend.entity.PortfolioPageContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioPageContentRepository extends JpaRepository<PortfolioPageContent, Short> {

    default Optional<PortfolioPageContent> findSingleton() {
        return findById((short) 1);
    }
}

