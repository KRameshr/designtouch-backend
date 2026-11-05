package com.designtouch.backend.controller;

import com.designtouch.backend.dto.ClientRequest;
import com.designtouch.backend.dto.ClientResponse;
import com.designtouch.backend.entity.Client;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.ClientRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientRepository repository;

    /**
     * Public endpoint: returns only active and featured clients for the homepage Trusted Partners section.
     */
    @GetMapping
    public List<ClientResponse> getAll() {
        return repository.findByIsActiveTrueAndIsFeaturedTrueOrderByDisplayOrderAsc()
                .stream()
                .map(ClientResponse::from)
                .toList();
    }

    /**
     * Admin endpoint: returns all client records (active, inactive, featured, non-featured).
     */
    @GetMapping("/admin/all")
    public List<ClientResponse> getAllForAdmin() {
        return repository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(ClientResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse create(@Valid @RequestBody ClientRequest request) {
        if (request.logoUrl() == null || request.logoUrl().trim().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client logo is required.");
        }

        Client client = Client.builder()
                .clientName(request.clientName().trim())
                .logoUrl(request.logoUrl().trim())
                .websiteUrl(request.websiteUrl() != null && !request.websiteUrl().trim().isBlank() ? request.websiteUrl().trim() : null)
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .isActive(request.isActive() != null ? request.isActive() : true)
                .isFeatured(request.isFeatured() != null ? request.isFeatured() : true)
                .build();
        return ClientResponse.from(repository.save(client));
    }

    @PutMapping("/{id}")
    public ClientResponse update(@PathVariable UUID id, @Valid @RequestBody ClientRequest request) {
        Client client = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + id));

        client.setClientName(request.clientName().trim());

        // Ensure edit operations do not accidentally remove an existing logo when no replacement is uploaded
        if (request.logoUrl() != null && !request.logoUrl().trim().isBlank()) {
            client.setLogoUrl(request.logoUrl().trim());
        }

        client.setWebsiteUrl(request.websiteUrl() != null && !request.websiteUrl().trim().isBlank() ? request.websiteUrl().trim() : null);

        if (request.displayOrder() != null) {
            client.setDisplayOrder(request.displayOrder());
        }
        if (request.isActive() != null) {
            client.setIsActive(request.isActive());
        }
        if (request.isFeatured() != null) {
            client.setIsFeatured(request.isFeatured());
        }

        return ClientResponse.from(repository.save(client));
    }

    @PatchMapping("/{id}/toggle-active")
    public ClientResponse toggleActive(@PathVariable UUID id) {
        Client client = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + id));
        client.setIsActive(!Boolean.TRUE.equals(client.getIsActive()));
        return ClientResponse.from(repository.save(client));
    }

    @PatchMapping("/{id}/toggle-featured")
    public ClientResponse toggleFeatured(@PathVariable UUID id) {
        Client client = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + id));
        client.setIsFeatured(!Boolean.TRUE.equals(client.getIsFeatured()));
        return ClientResponse.from(repository.save(client));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Client not found: " + id);
        }
        repository.deleteById(id);
    }
}
