package com.designtouch.backend.controller;

import com.designtouch.backend.dto.TestimonialRequest;
import com.designtouch.backend.dto.TestimonialResponse;
import com.designtouch.backend.entity.Testimonial;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.TestimonialRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/testimonials")
@RequiredArgsConstructor
public class TestimonialController {

    private final TestimonialRepository repository;

    @GetMapping
    public List<TestimonialResponse> getAll() {
        return repository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(TestimonialResponse::from)
                .toList();
    }

    @GetMapping("/admin/all")
    public List<TestimonialResponse> getAllForAdmin() {
        return repository.findAll().stream().map(TestimonialResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TestimonialResponse create(@Valid @RequestBody TestimonialRequest request) {
        Testimonial testimonial = Testimonial.builder()
                .quote(request.quote())
                .authorName(request.authorName())
                .authorRole(request.authorRole())
                .authorCompany(request.authorCompany())
                .authorAvatarUrl(request.authorAvatarUrl())
                .rating(request.rating())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();
        return TestimonialResponse.from(repository.save(testimonial));
    }

    @PutMapping("/{id}")
    public TestimonialResponse update(@PathVariable UUID id, @Valid @RequestBody TestimonialRequest request) {
        Testimonial testimonial = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonial not found: " + id));

        testimonial.setQuote(request.quote());
        testimonial.setAuthorName(request.authorName());
        testimonial.setAuthorRole(request.authorRole());
        testimonial.setAuthorCompany(request.authorCompany());
        testimonial.setAuthorAvatarUrl(request.authorAvatarUrl());
        testimonial.setRating(request.rating());
        if (request.displayOrder() != null) testimonial.setDisplayOrder(request.displayOrder());
        if (request.isActive() != null) testimonial.setIsActive(request.isActive());

        return TestimonialResponse.from(repository.save(testimonial));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Testimonial not found: " + id);
        }
        repository.deleteById(id);
    }
}
