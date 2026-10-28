package com.designtouch.backend.controller;

import com.designtouch.backend.dto.AddOnRequest;
import com.designtouch.backend.dto.AddOnResponse;
import com.designtouch.backend.entity.AddOn;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.AddOnRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/addons")
@RequiredArgsConstructor
public class AddOnController {

    private final AddOnRepository repository;

    // Public: returns active add-ons only
    @GetMapping
    public List<AddOnResponse> getAll() {
        return repository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(AddOnResponse::from)
                .toList();
    }

    // ---------- Admin Endpoints ----------

    @GetMapping("/admin/all")
    public List<AddOnResponse> getAllForAdmin() {
        return repository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(AddOnResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddOnResponse create(@Valid @RequestBody AddOnRequest request) {
        AddOn addOn = AddOn.builder()
                .name(request.name())
                .icon(request.icon())
                .priceLabel(request.priceLabel())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .isActive(request.isActive() != null ? request.isActive() : true)
                .customIconUrl(request.customIconUrl())
                .customIconPublicId(request.customIconPublicId())
                .build();

        return AddOnResponse.from(repository.save(addOn));
    }

    @PutMapping("/{id}")
    public AddOnResponse update(@PathVariable UUID id, @Valid @RequestBody AddOnRequest request) {
        AddOn addOn = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Add-on not found: " + id));

        addOn.setName(request.name());
        addOn.setIcon(request.icon());
        addOn.setPriceLabel(request.priceLabel());
        if (request.displayOrder() != null) addOn.setDisplayOrder(request.displayOrder());
        if (request.isActive() != null) addOn.setIsActive(request.isActive());
        addOn.setCustomIconUrl(request.customIconUrl());
        addOn.setCustomIconPublicId(request.customIconPublicId());

        return AddOnResponse.from(repository.save(addOn));
    }

    @PatchMapping("/{id}/toggle-active")
    public AddOnResponse toggleActive(@PathVariable UUID id) {
        AddOn addOn = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Add-on not found: " + id));
        boolean current = addOn.getIsActive() != null ? addOn.getIsActive() : true;
        addOn.setIsActive(!current);
        return AddOnResponse.from(repository.save(addOn));
    }

    @PutMapping("/reorder")
    public List<AddOnResponse> reorder(@RequestBody List<UUID> orderedIds) {
        if (orderedIds != null) {
            for (int i = 0; i < orderedIds.size(); i++) {
                UUID id = orderedIds.get(i);
                int order = i + 1;
                repository.findById(id).ifPresent(a -> {
                    a.setDisplayOrder(order);
                    repository.save(a);
                });
            }
        }
        return repository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(AddOnResponse::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Add-on not found: " + id);
        }
        repository.deleteById(id);
    }
}
