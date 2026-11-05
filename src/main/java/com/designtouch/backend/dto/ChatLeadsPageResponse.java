package com.designtouch.backend.dto;

import java.util.List;

public record ChatLeadsPageResponse(
        List<AdminChatLeadSummaryDto> leads,
        ChatLeadsDashboardStats stats,
        int currentPage,
        int totalPages,
        long totalElements
) {}

