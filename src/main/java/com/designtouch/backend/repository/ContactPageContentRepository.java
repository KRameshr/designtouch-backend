package com.designtouch.backend.repository;

import com.designtouch.backend.entity.ContactPageContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactPageContentRepository extends JpaRepository<ContactPageContent, Short> {
}

