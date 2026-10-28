package com.designtouch.backend.controller;

import com.designtouch.backend.dto.ProcessStepRequest;
import com.designtouch.backend.dto.ProcessStepResponse;
import com.designtouch.backend.entity.ProcessStep;
import com.designtouch.backend.entity.enums.PageType;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.ProcessStepRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/process-steps")
@RequiredArgsConstructor
public class ProcessStepController {

    private final ProcessStepRepository repository;

    // e.g. GET /api/process-steps?pageType=SERVICES (Public read — only active)
    @GetMapping
    public List<ProcessStepResponse> getByPageType(@RequestParam PageType pageType) {
        return repository.findByPageTypeAndIsActiveTrueOrderByStepNumberAsc(pageType)
                .stream()
                .map(ProcessStepResponse::from)
                .toList();
    }

    // ---------- Admin Endpoints ----------

    @GetMapping("/admin/all")
    public List<ProcessStepResponse> getAllForAdmin(@RequestParam(required = false, defaultValue = "SERVICES") PageType pageType) {
        return repository.findByPageTypeOrderByStepNumberAsc(pageType)
                .stream()
                .map(ProcessStepResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProcessStepResponse create(@Valid @RequestBody ProcessStepRequest request) {
        ProcessStep step = ProcessStep.builder()
                .pageType(request.pageType() != null ? request.pageType() : PageType.SERVICES)
                .stepNumber(request.stepNumber())
                .title(request.title())
                .description(request.description())
                .icon(request.icon())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : (int) request.stepNumber())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();
        return ProcessStepResponse.from(repository.save(step));
    }

    @PutMapping("/{id}")
    public ProcessStepResponse update(@PathVariable UUID id, @Valid @RequestBody ProcessStepRequest request) {
        ProcessStep step = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Process step not found: " + id));

        if (request.pageType() != null) step.setPageType(request.pageType());
        if (request.stepNumber() != null) step.setStepNumber(request.stepNumber());
        step.setTitle(request.title());
        step.setDescription(request.description());
        step.setIcon(request.icon());
        if (request.displayOrder() != null) step.setDisplayOrder(request.displayOrder());
        if (request.isActive() != null) step.setIsActive(request.isActive());

        return ProcessStepResponse.from(repository.save(step));
    }

    @PatchMapping("/{id}/toggle-active")
    public ProcessStepResponse toggleActive(@PathVariable UUID id) {
        ProcessStep step = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Process step not found: " + id));
        boolean current = step.getIsActive() != null ? step.getIsActive() : true;
        step.setIsActive(!current);
        return ProcessStepResponse.from(repository.save(step));
    }

    @PutMapping("/reorder")
    public List<ProcessStepResponse> reorder(@RequestBody List<UUID> orderedIds) {
        if (orderedIds != null) {
            for (int i = 0; i < orderedIds.size(); i++) {
                UUID id = orderedIds.get(i);
                short order = (short) (i + 1);
                repository.findById(id).ifPresent(s -> {
                    s.setStepNumber(order);
                    s.setDisplayOrder((int) order);
                    repository.save(s);
                });
            }
        }
        return repository.findAll()
                .stream()
                .map(ProcessStepResponse::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Process step not found: " + id);
        }
        repository.deleteById(id);
    }
}
