package com.designtouch.backend.controller;

import com.designtouch.backend.dto.ToolResponse;
import com.designtouch.backend.repository.ToolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolController {

    private final ToolRepository repository;

    @GetMapping
    public List<ToolResponse> getAll() {
        return repository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(ToolResponse::from)
                .toList();
    }
}
