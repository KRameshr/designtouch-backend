package com.designtouch.backend.service;

import com.designtouch.backend.dto.*;
import com.designtouch.backend.entity.ChatLead;
import com.designtouch.backend.repository.ChatLeadRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminChatLeadsService {

    private final ChatLeadRepository leadRepository;
    private final ExcelExportService excelExportService;

    @Transactional(readOnly = true)
    public ChatLeadsDashboardStats getDashboardStats() {
        long totalLeads = leadRepository.count();
        long newLeads = leadRepository.countByConversationStatusIgnoreCase("ACTIVE")
                + leadRepository.countByConversationStatusIgnoreCase("NEW");

        OffsetDateTime startOfToday = LocalDate.now(ZoneOffset.UTC).atStartOfDay().atOffset(ZoneOffset.UTC);
        long leadsToday = leadRepository.countByCreatedAtGreaterThanEqual(startOfToday);

        return new ChatLeadsDashboardStats(totalLeads, newLeads, leadsToday);
    }

    @Transactional(readOnly = true)
    public ChatLeadsPageResponse getLeads(
            String search,
            String status,
            String dateRange,
            String sortDirection,
            int page,
            int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = size > 0 && size <= 100 ? size : 15;

        Sort sort = switch (sortDirection != null ? sortDirection.toLowerCase() : "newest") {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);

        Specification<ChatLead> spec = buildSpecification(search, status, dateRange);
        Page<ChatLead> leadPage = leadRepository.findAll(spec, pageable);

        List<AdminChatLeadSummaryDto> summaryList = leadPage.getContent().stream().map(lead ->
                new AdminChatLeadSummaryDto(
                        lead.getId(),
                        lead.getName(),
                        lead.getEmail(),
                        lead.getConversationStatus(),
                        lead.getSource(),
                        lead.getCreatedAt(),
                        lead.getUpdatedAt()
                )
        ).toList();

        ChatLeadsDashboardStats stats = getDashboardStats();

        return new ChatLeadsPageResponse(
                summaryList,
                stats,
                leadPage.getNumber(),
                leadPage.getTotalPages(),
                leadPage.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public AdminChatLeadDetailResponse getLeadDetail(UUID id) {
        ChatLead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat lead not found with ID: " + id));

        return new AdminChatLeadDetailResponse(
                lead.getId(),
                lead.getName(),
                lead.getEmail(),
                lead.getConversationStatus(),
                lead.getSource(),
                lead.getCreatedAt(),
                lead.getUpdatedAt()
        );
    }

    @Transactional
    public AdminChatLeadSummaryDto updateLeadStatus(UUID id, String newStatus) {
        ChatLead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat lead not found with ID: " + id));

        String sanitized = newStatus != null ? newStatus.trim().toUpperCase() : "ACTIVE";
        lead.setConversationStatus(sanitized);
        lead.setUpdatedAt(OffsetDateTime.now());
        lead = leadRepository.save(lead);

        return new AdminChatLeadSummaryDto(
                lead.getId(),
                lead.getName(),
                lead.getEmail(),
                lead.getConversationStatus(),
                lead.getSource(),
                lead.getCreatedAt(),
                lead.getUpdatedAt()
        );
    }

    @Transactional
    public void deleteLead(UUID id) {
        if (!leadRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat lead not found with ID: " + id);
        }
        leadRepository.deleteById(id);
        log.info("Admin deleted chat lead ID: {}", id);
    }

    @Transactional
    public int bulkDeleteLeads(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        List<ChatLead> toDelete = leadRepository.findAllById(ids);
        leadRepository.deleteAll(toDelete);
        log.info("Admin bulk deleted {} chat leads", toDelete.size());
        return toDelete.size();
    }

    @Transactional(readOnly = true)
    public byte[] exportLeadsToExcel(
            List<UUID> specificIds,
            String search,
            String status,
            String dateRange
    ) throws IOException {
        List<ChatLead> leads;
        if (specificIds != null && !specificIds.isEmpty()) {
            leads = leadRepository.findAllById(specificIds);
        } else {
            Specification<ChatLead> spec = buildSpecification(search, status, dateRange);
            leads = leadRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        List<AdminChatLeadSummaryDto> summaries = leads.stream().map(lead ->
                new AdminChatLeadSummaryDto(
                        lead.getId(),
                        lead.getName(),
                        lead.getEmail(),
                        lead.getConversationStatus(),
                        lead.getSource(),
                        lead.getCreatedAt(),
                        lead.getUpdatedAt()
                )
        ).toList();

        return excelExportService.generateChatLeadsWorkbook(summaries);
    }

    private Specification<ChatLead> buildSpecification(String search, String status, String dateRange) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by name or email
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), pattern);
                Predicate emailLike = cb.like(cb.lower(root.get("email")), pattern);
                predicates.add(cb.or(nameLike, emailLike));
            }

            // Filter by status
            if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                predicates.add(cb.equal(cb.upper(root.get("conversationStatus")), status.trim().toUpperCase()));
            }

            // Filter by date range
            if (dateRange != null && !dateRange.isBlank() && !"ALL".equalsIgnoreCase(dateRange)) {
                OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
                OffsetDateTime threshold = switch (dateRange.toUpperCase()) {
                    case "TODAY" -> LocalDate.now(ZoneOffset.UTC).atStartOfDay().atOffset(ZoneOffset.UTC);
                    case "LAST_7_DAYS" -> now.minusDays(7);
                    case "LAST_30_DAYS" -> now.minusDays(30);
                    default -> null;
                };
                if (threshold != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), threshold));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
