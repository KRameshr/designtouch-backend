package com.designtouch.backend.repository;

import com.designtouch.backend.entity.BlogPageContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogPageContentRepository extends JpaRepository<BlogPageContent, Short> {
}
