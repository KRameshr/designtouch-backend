package com.designtouch.backend.controller;

import com.designtouch.backend.dto.CompanyValueResponse;
import com.designtouch.backend.repository.CompanyValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/values")
@RequiredArgsConstructor
public class CompanyValueController {

    private final CompanyValueRepository repository;

    @GetMapping
    public List<CompanyValueResponse> getAll() {
        return repository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(CompanyValueResponse::from)
                .toList();
    }
}
