package com.designtouch.backend.controller;

import com.designtouch.backend.dto.FaqResponse;
import com.designtouch.backend.entity.enums.FaqCategory;
import com.designtouch.backend.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqRepository repository;

    // e.g. GET /api/faqs?category=PRICING or ?category=GENERAL
    @GetMapping
    public List<FaqResponse> getByCategory(@RequestParam FaqCategory category) {
        return repository.findByCategoryOrderByDisplayOrderAsc(category)
                .stream()
                .map(FaqResponse::from)
                .toList();
    }
}
