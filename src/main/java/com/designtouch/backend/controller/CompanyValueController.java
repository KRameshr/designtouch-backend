package com.designtouch.backend.controller;

import com.designtouch.backend.dto.CompanyValueRequest;
import com.designtouch.backend.dto.CompanyValueResponse;
import com.designtouch.backend.entity.CompanyValue;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.CompanyValueRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/values")
@RequiredArgsConstructor
public class CompanyValueController {

    private final CompanyValueRepository repository;

    @GetMapping
    public List<CompanyValueResponse> getAll() {
        return repository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(CompanyValueResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyValueResponse create(@Valid @RequestBody CompanyValueRequest request) {
        CompanyValue value = CompanyValue.builder()
                .title(request.title())
                .description(request.description())
                .icon(request.icon())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .build();
        return CompanyValueResponse.from(repository.save(value));
    }

    @PutMapping("/{id}")
    public CompanyValueResponse update(@PathVariable UUID id, @Valid @RequestBody CompanyValueRequest request) {
        CompanyValue value = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company value not found: " + id));

        value.setTitle(request.title());
        value.setDescription(request.description());
        value.setIcon(request.icon());
        if (request.displayOrder() != null) {
            value.setDisplayOrder(request.displayOrder());
        }

        return CompanyValueResponse.from(repository.save(value));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Company value not found: " + id);
        }
        repository.deleteById(id);
    }
}
