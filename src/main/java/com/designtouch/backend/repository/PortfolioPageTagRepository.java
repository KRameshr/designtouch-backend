package com.designtouch.backend.repository;

import com.designtouch.backend.entity.PortfolioPageTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PortfolioPageTagRepository extends JpaRepository<PortfolioPageTag, UUID> {

    List<PortfolioPageTag> findAllByOrderByDisplayOrderAsc();

    List<PortfolioPageTag> findAllByIsActiveTrueOrderByDisplayOrderAsc();
}

