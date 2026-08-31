package com.designtouch.backend.controller;

import com.designtouch.backend.dto.ServiceRequest;
import com.designtouch.backend.dto.ServiceResponse;
import com.designtouch.backend.entity.Service;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.ServiceRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceRepository repository;

    // ---------- Public reads ----------

    @GetMapping
    public List<ServiceResponse> getAll() {
        return repository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(ServiceResponse::from)
                .toList();
    }

    // ---------- Admin (requires JWT — see SecurityConfig) ----------

    @GetMapping("/admin/all")
    public List<ServiceResponse> getAllForAdmin() {
        return repository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(ServiceResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse create(@Valid @RequestBody ServiceRequest request) {
        Service service = Service.builder()
                .icon(request.icon())
                .title(request.title())
                .description(request.description())
                .features(request.features())
                .colorTheme(request.colorTheme())
                .slug(request.slug())
                .orderNumber(request.orderNumber())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .build();
        return ServiceResponse.from(repository.save(service));
    }

    @PutMapping("/{id}")
    public ServiceResponse update(@PathVariable UUID id, @Valid @RequestBody ServiceRequest request) {
        Service service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + id));

        service.setIcon(request.icon());
        service.setTitle(request.title());
        service.setDescription(request.description());
        service.setFeatures(request.features());
        service.setColorTheme(request.colorTheme());
        service.setSlug(request.slug());
        service.setOrderNumber(request.orderNumber());
        if (request.displayOrder() != null) service.setDisplayOrder(request.displayOrder());

        return ServiceResponse.from(repository.save(service));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Service not found: " + id);
        }
        repository.deleteById(id);
    }

    // ---------- Public detail (must stay LAST — /{slug} would otherwise
    // greedily match /admin/all above) ----------

    // "LEARN MORE" -> /services/{slug} detail page
    @GetMapping("/{slug}")
    public ServiceResponse getBySlug(@PathVariable String slug) {
        return repository.findBySlug(slug)
                .map(ServiceResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + slug));
    }
}
