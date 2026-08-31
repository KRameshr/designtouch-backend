package com.designtouch.backend.controller;

import com.designtouch.backend.dto.CompanyInfoResponse;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.CompanyInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/company-info")
@RequiredArgsConstructor
public class CompanyInfoController {

    private final CompanyInfoRepository repository;

    @GetMapping
    public CompanyInfoResponse get() {
        return repository.findById((short) 1)
                .map(CompanyInfoResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Company info not set up yet"));
    }
}
