package com.designtouch.backend.repository;

import com.designtouch.backend.entity.ChatLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatLeadRepository extends JpaRepository<ChatLead, UUID>, JpaSpecificationExecutor<ChatLead> {

    Optional<ChatLead> findFirstByEmailIgnoreCaseOrderByCreatedAtDesc(String email);

    long countByConversationStatusIgnoreCase(String status);

    long countByCreatedAtGreaterThanEqual(OffsetDateTime threshold);

    List<ChatLead> findByIdIn(List<UUID> ids);
}
