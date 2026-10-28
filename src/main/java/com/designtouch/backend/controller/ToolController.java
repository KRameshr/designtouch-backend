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
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolController {

    private static final Pattern LOCAL_TOOL_ICON_PATH =
            Pattern.compile("^/assets/tools/[a-zA-Z0-9._-]+\\.(svg|png|webp)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern LOCAL_TOOL_ICON_FILENAME =
            Pattern.compile("^[a-zA-Z0-9._-]+\\.(svg|png|webp)$", Pattern.CASE_INSENSITIVE);

    private final ToolRepository repository;

    private String normalizeLogoUrl(String rawLogoUrl) {
        if (rawLogoUrl == null) {
            return "";
        }
        String trimmed = rawLogoUrl.trim();
        if (trimmed.isEmpty() || "none".equalsIgnoreCase(trimmed)) {
            return "";
        }
        if (trimmed.contains("..")) {
            throw new IllegalArgumentException("Invalid icon asset path.");
        }
        if (LOCAL_TOOL_ICON_PATH.matcher(trimmed).matches()) {
            return trimmed;
        }
        if (LOCAL_TOOL_ICON_FILENAME.matcher(trimmed).matches()) {
            return "/assets/tools/" + trimmed;
        }
        throw new IllegalArgumentException(
                "Tool logoUrl must reference a local asset in /assets/tools/ (.svg, .png, .webp) or be empty."
        );
    }

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
        String normalizedLogoUrl = normalizeLogoUrl(request.logoUrl());
        Tool tool = Tool.builder()
                .name(request.name().trim())
                .iconKey(request.iconKey() != null ? request.iconKey().trim() : null)
                .logoUrl(normalizedLogoUrl)
                .websiteUrl(request.websiteUrl() != null ? request.websiteUrl().trim() : null)
                .role(request.role() != null ? request.role().trim() : null)
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();
        return ToolResponse.from(repository.save(tool));
    }

    @PutMapping("/{id}")
    public ToolResponse update(@PathVariable UUID id, @Valid @RequestBody ToolRequest request) {
        Tool tool = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found: " + id));

        tool.setName(request.name().trim());
        tool.setIconKey(request.iconKey() != null ? request.iconKey().trim() : null);
        tool.setLogoUrl(normalizeLogoUrl(request.logoUrl()));
        tool.setWebsiteUrl(request.websiteUrl() != null ? request.websiteUrl().trim() : null);
        tool.setRole(request.role() != null ? request.role().trim() : null);
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
