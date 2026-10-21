package com.designtouch.backend.repository;

import com.designtouch.backend.entity.HeroContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HeroContentRepository extends JpaRepository<HeroContent, Short> {

    default Optional<HeroContent> findSingleton() {
        return findById((short) 1);
    }
}

