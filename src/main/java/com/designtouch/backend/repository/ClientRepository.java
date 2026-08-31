package com.designtouch.backend.repository;

import com.designtouch.backend.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    List<Client> findAllByOrderByDisplayOrderAsc();
}
