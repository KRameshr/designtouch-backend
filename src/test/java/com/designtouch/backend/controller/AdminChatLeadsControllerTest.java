package com.designtouch.backend.controller;

import com.designtouch.backend.dto.*;
import com.designtouch.backend.exception.GlobalExceptionHandler;
import com.designtouch.backend.service.AdminChatLeadsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminChatLeadsControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private AdminChatLeadsService leadsService;
    private final UUID sampleLeadId = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private final AtomicInteger deletedCounter = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        leadsService = new AdminChatLeadsService(null, null) {
            @Override
            public ChatLeadsDashboardStats getDashboardStats() {
                return new ChatLeadsDashboardStats(25, 8, 4);
            }

            @Override
            public ChatLeadsPageResponse getLeads(String search, String status, String dateRange, String sort, int page, int size) {
                AdminChatLeadSummaryDto lead = new AdminChatLeadSummaryDto(
                        sampleLeadId,
                        "John Wick",
                        "john@continental.com",
                        "ACTIVE",
                        "WEBSITE_CHATBOT",
                        OffsetDateTime.now().minusHours(2),
                        OffsetDateTime.now()
                );
                return new ChatLeadsPageResponse(
                        List.of(lead),
                        getDashboardStats(),
                        0,
                        1,
                        1
                );
            }

            @Override
            public AdminChatLeadDetailResponse getLeadDetail(UUID id) {
                if (!sampleLeadId.equals(id)) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found");
                }
                return new AdminChatLeadDetailResponse(
                        sampleLeadId,
                        "John Wick",
                        "john@continental.com",
                        "ACTIVE",
                        "WEBSITE_CHATBOT",
                        OffsetDateTime.now().minusHours(2),
                        OffsetDateTime.now()
                );
            }

            @Override
            public void deleteLead(UUID id) {
                if (!sampleLeadId.equals(id)) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found");
                }
                deletedCounter.incrementAndGet();
            }

            @Override
            public int bulkDeleteLeads(List<UUID> ids) {
                if (ids == null) return 0;
                deletedCounter.addAndGet(ids.size());
                return ids.size();
            }

            @Override
            public byte[] exportLeadsToExcel(List<UUID> specificIds, String search, String status, String dateRange) throws IOException {
                return "PK\u0003\u0004mock-xlsx-data".getBytes();
            }
        };

        AdminChatLeadsController controller = new AdminChatLeadsController(leadsService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("1. GET /api/admin/chat-leads returns paginated leads and dashboard stats")
    void getLeads_returnsPaginatedResponse() throws Exception {
        mockMvc.perform(get("/api/admin/chat-leads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leads", hasSize(1)))
                .andExpect(jsonPath("$.leads[0].name").value("John Wick"))
                .andExpect(jsonPath("$.leads[0].email").value("john@continental.com"))
                .andExpect(jsonPath("$.stats.totalLeads").value(25))
                .andExpect(jsonPath("$.stats.newLeads").value(8))
                .andExpect(jsonPath("$.stats.leadsToday").value(4));
    }

    @Test
    @DisplayName("2. GET /api/admin/chat-leads/stats returns summary metrics")
    void getStats_returnsMetrics() throws Exception {
        mockMvc.perform(get("/api/admin/chat-leads/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLeads").value(25))
                .andExpect(jsonPath("$.leadsToday").value(4));
    }

    @Test
    @DisplayName("3. GET /api/admin/chat-leads/{id} returns lead details without conversation history")
    void getLeadDetail_existingId_returnsDetail() throws Exception {
        mockMvc.perform(get("/api/admin/chat-leads/" + sampleLeadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleLeadId.toString()))
                .andExpect(jsonPath("$.name").value("John Wick"))
                .andExpect(jsonPath("$.email").value("john@continental.com"))
                .andExpect(jsonPath("$.conversationStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("4. GET /api/admin/chat-leads/{id} returns 404 for unknown lead")
    void getLeadDetail_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/admin/chat-leads/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("5. DELETE /api/admin/chat-leads/{id} removes lead and returns 204")
    void deleteLead_existingId_returns204() throws Exception {
        mockMvc.perform(delete("/api/admin/chat-leads/" + sampleLeadId))
                .andExpect(status().isNoContent());
        org.junit.jupiter.api.Assertions.assertEquals(1, deletedCounter.get());
    }

    @Test
    @DisplayName("6. POST /api/admin/chat-leads/bulk-delete deletes multiple leads")
    void bulkDelete_validIds_returnsDeletedCount() throws Exception {
        BulkDeleteRequest request = new BulkDeleteRequest(List.of(sampleLeadId, UUID.randomUUID()));

        mockMvc.perform(post("/api/admin/chat-leads/bulk-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.deletedCount").value(2));
    }

    @Test
    @DisplayName("7. GET /api/admin/chat-leads/export returns Excel file stream")
    void exportLeads_returnsExcelBytes() throws Exception {
        mockMvc.perform(get("/api/admin/chat-leads/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"designtouch-chat-leads-")))
                .andExpect(content().bytes("PK\u0003\u0004mock-xlsx-data".getBytes()));
    }
}
