package com.designtouch.backend.controller;

import com.designtouch.backend.dto.TeamMemberRequest;
import com.designtouch.backend.dto.TeamMemberResponse;
import com.designtouch.backend.entity.TeamMember;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.TeamMemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberRepository repository;

    @GetMapping
    public List<TeamMemberResponse> getAll() {
        return repository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(TeamMemberResponse::from)
                .toList();
    }

    @GetMapping("/admin/all")
    public List<TeamMemberResponse> getAllForAdmin() {
        return repository.findAll().stream().map(TeamMemberResponse::from).toList();
    }

    private String normalizeUrl(String url) {
        if (url == null) return null;
        String trimmed = url.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamMemberResponse create(@Valid @RequestBody TeamMemberRequest request) {
        TeamMember member = TeamMember.builder()
                .name(request.name().trim())
                .role(request.role().trim())
                .photoUrl(normalizeUrl(request.photoUrl()))
                .linkedinUrl(normalizeUrl(request.linkedinUrl()))
                .instagramUrl(normalizeUrl(request.instagramUrl()))
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();
        return TeamMemberResponse.from(repository.save(member));
    }

    @PutMapping("/{id}")
    public TeamMemberResponse update(@PathVariable UUID id, @Valid @RequestBody TeamMemberRequest request) {
        TeamMember member = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found: " + id));

        member.setName(request.name().trim());
        member.setRole(request.role().trim());
        member.setPhotoUrl(normalizeUrl(request.photoUrl()));
        member.setLinkedinUrl(normalizeUrl(request.linkedinUrl()));
        member.setInstagramUrl(normalizeUrl(request.instagramUrl()));
        if (request.displayOrder() != null) member.setDisplayOrder(request.displayOrder());
        if (request.isActive() != null) member.setIsActive(request.isActive());

        return TeamMemberResponse.from(repository.save(member));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Team member not found: " + id);
        }
        repository.deleteById(id);
    }
}
