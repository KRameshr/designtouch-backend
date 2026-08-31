package com.designtouch.backend.repository;

import com.designtouch.backend.entity.AddOn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddOnRepository extends JpaRepository<AddOn, UUID> {

    List<AddOn> findAllByOrderByDisplayOrderAsc();
}
