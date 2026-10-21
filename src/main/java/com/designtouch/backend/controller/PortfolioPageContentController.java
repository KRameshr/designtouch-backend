package com.designtouch.backend.controller;

import com.designtouch.backend.dto.PortfolioPageContentRequest;
import com.designtouch.backend.dto.PortfolioPageContentResponse;
import com.designtouch.backend.dto.PortfolioPageTagRequest;
import com.designtouch.backend.dto.PortfolioPageTagResponse;
import com.designtouch.backend.service.PortfolioPageContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portfolio-page")
@RequiredArgsConstructor
public class PortfolioPageContentController {

    private final PortfolioPageContentService service;

    // ---------- Intro Content (Singleton) ----------

    @GetMapping
    public PortfolioPageContentResponse getContent() {
        return service.getPortfolioPageContent();
    }

    @PutMapping
    public PortfolioPageContentResponse updateContent(@Valid @RequestBody PortfolioPageContentRequest request) {
        return service.updatePortfolioPageContent(request);
    }

    // ---------- Intro Tags ----------

    @GetMapping("/tags")
    public List<PortfolioPageTagResponse> getTags(@RequestParam(required = false, defaultValue = "false") boolean all) {
        return service.getTags(all);
    }

    @PostMapping("/tags")
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioPageTagResponse createTag(@Valid @RequestBody PortfolioPageTagRequest request) {
        return service.createTag(request);
    }

    @PutMapping("/tags/{id}")
    public PortfolioPageTagResponse updateTag(
            @PathVariable UUID id,
            @Valid @RequestBody PortfolioPageTagRequest request) {
        return service.updateTag(id, request);
    }

    @DeleteMapping("/tags/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(@PathVariable UUID id) {
        service.deleteTag(id);
    }
}

