package com.designtouch.backend.service;

import com.designtouch.backend.dto.AdminChatLeadDetailResponse;
import com.designtouch.backend.dto.AdminChatLeadSummaryDto;
import com.designtouch.backend.dto.ChatLeadsDashboardStats;
import com.designtouch.backend.dto.ChatLeadsPageResponse;
import com.designtouch.backend.entity.ChatLead;
import com.designtouch.backend.repository.ChatLeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminChatLeadsServiceTest {

    private final Map<UUID, ChatLead> leads = new HashMap<>();

    private ChatLeadRepository leadRepository;
    private ExcelExportService excelExportService;
    private AdminChatLeadsService service;

    private UUID leadId;
    private ChatLead sampleLead;

    @BeforeEach
    void setUp() {
        leads.clear();

        leadId = UUID.randomUUID();
        sampleLead = ChatLead.builder()
                .id(leadId)
                .name("Alice Walker")
                .email("alice@example.com")
                .conversationStatus("ACTIVE")
                .source("WEBSITE_CHATBOT")
                .createdAt(OffsetDateTime.now().minusHours(3))
                .updatedAt(OffsetDateTime.now())
                .build();
        leads.put(leadId, sampleLead);

        leadRepository = (ChatLeadRepository) Proxy.newProxyInstance(
                ChatLeadRepository.class.getClassLoader(),
                new Class<?>[]{ChatLeadRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("count".equals(name)) {
                        return (long) leads.size();
                    }
                    if ("countByConversationStatusIgnoreCase".equals(name)) {
                        String st = (String) args[0];
                        return leads.values().stream()
                                .filter(l -> st.equalsIgnoreCase(l.getConversationStatus()))
                                .count();
                    }
                    if ("countByCreatedAtGreaterThanEqual".equals(name)) {
                        OffsetDateTime threshold = (OffsetDateTime) args[0];
                        return leads.values().stream()
                                .filter(l -> l.getCreatedAt() != null && !l.getCreatedAt().isBefore(threshold))
                                .count();
                    }
                    if ("findAll".equals(name) && args != null && args.length == 2) {
                        if (args[1] instanceof org.springframework.data.domain.Pageable) {
                            return new PageImpl<>(new ArrayList<>(leads.values()));
                        }
                        if (args[1] instanceof org.springframework.data.domain.Sort) {
                            return new ArrayList<>(leads.values());
                        }
                        return new PageImpl<>(new ArrayList<>(leads.values()));
                    }
                    if ("findById".equals(name)) {
                        UUID id = (UUID) args[0];
                        return Optional.ofNullable(leads.get(id));
                    }
                    if ("existsById".equals(name)) {
                        UUID id = (UUID) args[0];
                        return leads.containsKey(id);
                    }
                    if ("deleteById".equals(name)) {
                        UUID id = (UUID) args[0];
                        leads.remove(id);
                        return null;
                    }
                    if ("findAllById".equals(name)) {
                        Collection<UUID> ids = (Collection<UUID>) args[0];
                        List<ChatLead> list = new ArrayList<>();
                        for (UUID id : ids) {
                            if (leads.containsKey(id)) list.add(leads.get(id));
                        }
                        return list;
                    }
                    if ("deleteAll".equals(name)) {
                        Iterable<ChatLead> entities = (Iterable<ChatLead>) args[0];
                        for (ChatLead l : entities) {
                            leads.remove(l.getId());
                        }
                        return null;
                    }
                    if ("save".equals(name)) {
                        ChatLead l = (ChatLead) args[0];
                        if (l.getId() == null) l.setId(UUID.randomUUID());
                        leads.put(l.getId(), l);
                        return l;
                    }
                    return null;
                }
        );

        excelExportService = new ExcelExportService();
        service = new AdminChatLeadsService(leadRepository, excelExportService);
    }

    @Test
    @DisplayName("1. getDashboardStats computes accurate summary counts")
    void getDashboardStats_returnsMetrics() {
        ChatLeadsDashboardStats stats = service.getDashboardStats();

        assertThat(stats.totalLeads()).isEqualTo(1);
        assertThat(stats.newLeads()).isEqualTo(1);
        assertThat(stats.leadsToday()).isEqualTo(1);
    }

    @Test
    @DisplayName("2. getLeads returns paginated summaries with lead details")
    void getLeads_returnsPaginatedList() {
        ChatLeadsPageResponse page = service.getLeads(null, "ALL", "ALL", "newest", 0, 15);

        assertThat(page.leads()).hasSize(1);
        AdminChatLeadSummaryDto item = page.leads().get(0);
        assertThat(item.id()).isEqualTo(leadId);
        assertThat(item.name()).isEqualTo("Alice Walker");
        assertThat(item.email()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("3. getLeadDetail returns lead information without conversations")
    void getLeadDetail_returnsDetail() {
        AdminChatLeadDetailResponse detail = service.getLeadDetail(leadId);

        assertThat(detail.id()).isEqualTo(leadId);
        assertThat(detail.name()).isEqualTo("Alice Walker");
        assertThat(detail.email()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("4. getLeadDetail throws 404 for missing ID")
    void getLeadDetail_notFound() {
        UUID randomId = UUID.randomUUID();
        assertThatThrownBy(() -> service.getLeadDetail(randomId))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("5. updateLeadStatus persists new conversationStatus")
    void updateLeadStatus_success() {
        AdminChatLeadSummaryDto updated = service.updateLeadStatus(leadId, "RESOLVED");

        assertThat(updated.conversationStatus()).isEqualTo("RESOLVED");
        assertThat(leads.get(leadId).getConversationStatus()).isEqualTo("RESOLVED");
    }

    @Test
    @DisplayName("6. deleteLead removes existing record")
    void deleteLead_removesRecord() {
        service.deleteLead(leadId);

        assertThat(leads).doesNotContainKey(leadId);
    }

    @Test
    @DisplayName("7. bulkDeleteLeads removes specified records")
    void bulkDeleteLeads_removesMultiple() {
        UUID secondId = UUID.randomUUID();
        leads.put(secondId, ChatLead.builder().id(secondId).name("Bob").email("bob@example.com").build());

        int count = service.bulkDeleteLeads(List.of(leadId, secondId));

        assertThat(count).isEqualTo(2);
        assertThat(leads).isEmpty();
    }

    @Test
    @DisplayName("8. exportLeadsToExcel generates non-empty Excel byte array")
    void exportLeadsToExcel_returnsBytes() throws IOException {
        byte[] bytes = service.exportLeadsToExcel(null, null, "ALL", "ALL");

        assertThat(bytes).isNotNull();
        assertThat(bytes.length).isGreaterThan(100);
    }
}
