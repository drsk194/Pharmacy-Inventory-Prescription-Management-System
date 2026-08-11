package com.pharmacy.pipms.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// Structure only — see Module 19 notes. No real backup job runs.
@Getter
@AllArgsConstructor
public class BackupStatusResponse {
    private String status;
    private LocalDateTime lastBackupAt;
    private String frequency;
    private String note;
}