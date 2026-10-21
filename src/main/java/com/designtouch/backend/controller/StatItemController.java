package com.designtouch.backend.controller;

import com.designtouch.backend.dto.StatItemRequest;
import com.designtouch.backend.dto.StatItemResponse;
import com.designtouch.backend.entity.StatItem;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.StatItemRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatItemController {

    private final StatItemRepository repository;

    @GetMapping
    public List<StatItemResponse> getAll() {
        return repository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(StatItemResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StatItemResponse create(@Valid @RequestBody StatItemRequest request) {
        StatItem stat = StatItem.builder()
                .label(request.label())
                .value(request.value())
                .suffix(request.suffix())
                .description(request.description() != null ? request.description() : request.label())
                .icon(request.icon())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .build();
        return StatItemResponse.from(repository.save(stat));
    }

    @PutMapping("/{id}")
    public StatItemResponse update(@PathVariable UUID id, @Valid @RequestBody StatItemRequest request) {
        StatItem stat = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stat item not found: " + id));

        stat.setLabel(request.label());
        stat.setValue(request.value());
        stat.setSuffix(request.suffix());
        stat.setDescription(request.description() != null ? request.description() : request.label());
        stat.setIcon(request.icon());
        if (request.displayOrder() != null) {
            stat.setDisplayOrder(request.displayOrder());
        }

        return StatItemResponse.from(repository.save(stat));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Stat item not found: " + id);
        }
        repository.deleteById(id);
    }
}
