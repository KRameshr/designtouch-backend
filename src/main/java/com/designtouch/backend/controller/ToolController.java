package com.designtouch.backend.controller;

import com.designtouch.backend.dto.ToolRequest;
import com.designtouch.backend.dto.ToolResponse;
import com.designtouch.backend.entity.Tool;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.ToolRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolController {

    private final ToolRepository repository;

    // Public read (only active tools)
    @GetMapping
    public List<ToolResponse> getAll() {
        return repository.findAllByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(ToolResponse::from)
                .toList();
    }

    // ---------- Admin Endpoints ----------

    @GetMapping("/admin/all")
    public List<ToolResponse> getAllForAdmin() {
        return repository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(ToolResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ToolResponse create(@Valid @RequestBody ToolRequest request) {
        Tool tool = Tool.builder()
                .name(request.name())
                .iconKey(request.iconKey())
                .logoUrl(request.logoUrl())
                .websiteUrl(request.websiteUrl())
                .role(request.role())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();
        return ToolResponse.from(repository.save(tool));
    }

    @PutMapping("/{id}")
    public ToolResponse update(@PathVariable UUID id, @Valid @RequestBody ToolRequest request) {
        Tool tool = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found: " + id));

        tool.setName(request.name());
        tool.setIconKey(request.iconKey());
        tool.setLogoUrl(request.logoUrl());
        tool.setWebsiteUrl(request.websiteUrl());
        tool.setRole(request.role());
        if (request.displayOrder() != null) tool.setDisplayOrder(request.displayOrder());
        if (request.isActive() != null) tool.setIsActive(request.isActive());

        return ToolResponse.from(repository.save(tool));
    }

    @PatchMapping("/{id}/toggle-active")
    public ToolResponse toggleActive(@PathVariable UUID id) {
        Tool tool = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found: " + id));
        boolean current = tool.getIsActive() != null ? tool.getIsActive() : true;
        tool.setIsActive(!current);
        return ToolResponse.from(repository.save(tool));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Tool not found: " + id);
        }
        repository.deleteById(id);
    }
}
