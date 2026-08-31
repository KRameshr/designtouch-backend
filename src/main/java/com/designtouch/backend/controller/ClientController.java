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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientRepository repository;

    @GetMapping
    public List<ClientResponse> getAll() {
        return repository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(ClientResponse::from)
                .toList();
    }

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
        Client client = Client.builder()
                .clientName(request.clientName())
                .logoUrl(request.logoUrl())
                .websiteUrl(request.websiteUrl())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .build();
        return ClientResponse.from(repository.save(client));
    }

    @PutMapping("/{id}")
    public ClientResponse update(@PathVariable UUID id, @Valid @RequestBody ClientRequest request) {
        Client client = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + id));

        client.setClientName(request.clientName());
        client.setLogoUrl(request.logoUrl());
        client.setWebsiteUrl(request.websiteUrl());
        if (request.displayOrder() != null) client.setDisplayOrder(request.displayOrder());

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
