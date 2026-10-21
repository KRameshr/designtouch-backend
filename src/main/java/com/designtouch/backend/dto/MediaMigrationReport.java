package com.designtouch.backend.dto;

import java.util.List;

public record MediaMigrationReport(
        int totalProjects,
        int migratedCount,
        int skippedCount,
        int errorCount,
        boolean dryRun,
        List<ProjectMediaInventoryItem> items
) {}

