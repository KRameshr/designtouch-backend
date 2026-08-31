package com.designtouch.backend.controller;

import com.designtouch.backend.dto.ProcessStepResponse;
import com.designtouch.backend.entity.enums.PageType;
import com.designtouch.backend.repository.ProcessStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/process-steps")
@RequiredArgsConstructor
public class ProcessStepController {

    private final ProcessStepRepository repository;

    // e.g. GET /api/process-steps?pageType=ABOUT
    @GetMapping
    public List<ProcessStepResponse> getByPageType(@RequestParam PageType pageType) {
        return repository.findByPageTypeOrderByStepNumberAsc(pageType)
                .stream()
                .map(ProcessStepResponse::from)
                .toList();
    }
}
