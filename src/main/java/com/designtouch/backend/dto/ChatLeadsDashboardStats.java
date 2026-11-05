package com.designtouch.backend.dto;

public record ChatLeadsDashboardStats(
        long totalLeads,
        long newLeads,
        long leadsToday
) {}
