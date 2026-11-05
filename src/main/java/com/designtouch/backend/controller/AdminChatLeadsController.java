package com.designtouch.backend.controller;

import com.designtouch.backend.dto.*;
import com.designtouch.backend.service.AdminChatLeadsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/chat-leads")
@RequiredArgsConstructor
public class AdminChatLeadsController {

    private final AdminChatLeadsService leadsService;

    @GetMapping
    public ChatLeadsPageResponse getLeads(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "ALL") String dateRange,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "15") int size
    ) {
        return leadsService.getLeads(search, status, dateRange, sort, page, size);
    }

    @GetMapping("/stats")
    public ChatLeadsDashboardStats getStats() {
        return leadsService.getDashboardStats();
    }

    @GetMapping("/{id}")
    public AdminChatLeadDetailResponse getLeadDetail(@PathVariable UUID id) {
        return leadsService.getLeadDetail(id);
    }

    @PatchMapping("/{id}/status")
    public AdminChatLeadSummaryDto updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body
    ) {
        String newStatus = body != null ? body.get("status") : "ACTIVE";
        return leadsService.updateLeadStatus(id, newStatus);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLead(@PathVariable UUID id) {
        leadsService.deleteLead(id);
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<Map<String, Object>> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
        int deletedCount = leadsService.bulkDeleteLeads(request.ids());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "deletedCount", deletedCount,
                "message", "Successfully deleted " + deletedCount + " leads."
        ));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportLeads(
            @RequestParam(required = false) List<UUID> ids,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "ALL") String dateRange
    ) throws IOException {
        byte[] excelBytes = leadsService.exportLeadsToExcel(ids, search, status, dateRange);

        String filename = "designtouch-chat-leads-" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentLength(excelBytes.length)
                .body(excelBytes);
    }
}

