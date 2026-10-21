package com.designtouch.backend.controller;

import com.designtouch.backend.dto.MediaMigrationReport;
import com.designtouch.backend.service.MediaMigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/media/migration")
@RequiredArgsConstructor
public class MediaMigrationController {

    private final MediaMigrationService migrationService;

    @GetMapping("/inventory")
    public ResponseEntity<MediaMigrationReport> getInventory() {
        return ResponseEntity.ok(migrationService.generateInventory());
    }

    @PostMapping("/run")
    public ResponseEntity<MediaMigrationReport> runMigration(
            @RequestParam(value = "dryRun", defaultValue = "true") boolean dryRun) {
        return ResponseEntity.ok(migrationService.migrateMedia(dryRun));
    }
}

