package com.designtouch.backend.repository;

import com.designtouch.backend.entity.CompanyValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompanyValueRepository extends JpaRepository<CompanyValue, UUID> {

    List<CompanyValue> findAllByOrderByDisplayOrderAsc();
}
