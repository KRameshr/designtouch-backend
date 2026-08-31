package com.designtouch.backend.repository;

import com.designtouch.backend.entity.StatItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StatItemRepository extends JpaRepository<StatItem, UUID> {

    List<StatItem> findAllByOrderByDisplayOrderAsc();
}
