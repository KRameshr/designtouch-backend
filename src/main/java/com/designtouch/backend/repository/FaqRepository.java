package com.designtouch.backend.repository;

import com.designtouch.backend.entity.Faq;
import com.designtouch.backend.entity.enums.FaqCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FaqRepository extends JpaRepository<Faq, UUID> {

    List<Faq> findByCategoryOrderByDisplayOrderAsc(FaqCategory category);
}
