package com.designtouch.backend.controller;

import com.designtouch.backend.dto.StatItemResponse;
import com.designtouch.backend.repository.StatItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
