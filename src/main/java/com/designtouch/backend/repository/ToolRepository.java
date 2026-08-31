package com.designtouch.backend.repository;

import com.designtouch.backend.entity.Tool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ToolRepository extends JpaRepository<Tool, UUID> {

    List<Tool> findAllByOrderByDisplayOrderAsc();
}
