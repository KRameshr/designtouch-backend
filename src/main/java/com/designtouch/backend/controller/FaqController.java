package com.designtouch.backend.controller;

import com.designtouch.backend.dto.FaqRequest;
import com.designtouch.backend.dto.FaqResponse;
import com.designtouch.backend.entity.Faq;
import com.designtouch.backend.entity.enums.FaqCategory;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.FaqRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqRepository repository;

    // Public read: returns active FAQs only
    // e.g. GET /api/faqs?category=PRICING or ?category=GENERAL
    @GetMapping
    public List<FaqResponse> getByCategory(@RequestParam FaqCategory category) {
        return repository.findByCategoryAndIsActiveTrueOrderByDisplayOrderAsc(category)
                .stream()
                .map(FaqResponse::from)
                .toList();
    }

    // ---------- Admin Endpoints ----------

    @GetMapping("/admin/all")
    public List<FaqResponse> getAllForAdmin(@RequestParam(required = false) FaqCategory category) {
        List<Faq> faqs = (category != null)
                ? repository.findByCategoryOrderByDisplayOrderAsc(category)
                : repository.findAllByOrderByDisplayOrderAsc();

        return faqs.stream()
                .map(FaqResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FaqResponse create(@Valid @RequestBody FaqRequest request) {
        Faq faq = Faq.builder()
                .question(request.question())
                .answer(request.answer())
                .category(request.category())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();

        return FaqResponse.from(repository.save(faq));
    }

    @PutMapping("/{id}")
    public FaqResponse update(@PathVariable UUID id, @Valid @RequestBody FaqRequest request) {
        Faq faq = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ not found: " + id));

        faq.setQuestion(request.question());
        faq.setAnswer(request.answer());
        if (request.category() != null) faq.setCategory(request.category());
        if (request.displayOrder() != null) faq.setDisplayOrder(request.displayOrder());
        if (request.isActive() != null) faq.setIsActive(request.isActive());

        return FaqResponse.from(repository.save(faq));
    }

    @PatchMapping("/{id}/toggle-active")
    public FaqResponse toggleActive(@PathVariable UUID id) {
        Faq faq = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ not found: " + id));
        boolean current = faq.getIsActive() != null ? faq.getIsActive() : true;
        faq.setIsActive(!current);
        return FaqResponse.from(repository.save(faq));
    }

    @PutMapping("/reorder")
    public List<FaqResponse> reorder(@RequestBody List<UUID> orderedIds) {
        if (orderedIds != null) {
            for (int i = 0; i < orderedIds.size(); i++) {
                UUID id = orderedIds.get(i);
                int order = i + 1;
                repository.findById(id).ifPresent(f -> {
                    f.setDisplayOrder(order);
                    repository.save(f);
                });
            }
        }
        return repository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(FaqResponse::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("FAQ not found: " + id);
        }
        repository.deleteById(id);
    }
}
