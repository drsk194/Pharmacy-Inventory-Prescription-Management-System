package com.pharmacy.pipms.admin.controller;

import com.pharmacy.pipms.admin.dto.*;
import com.pharmacy.pipms.admin.service.AdminSystemService;
import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.notification.service.LicenseExpiryAlertJob;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin System", description = "System health, integration/backup status, and compliance dashboard")
public class AdminSystemController {

    private final AdminSystemService adminSystemService;
    private final LicenseExpiryAlertJob licenseExpiryAlertJob;

    @GetMapping("/system-health")
    @PreAuthorize("hasAuthority('SYSTEM_CONFIGURE')")
    public ApiResponse<SystemHealthResponse> systemHealth() {
        return ApiResponse.success(adminSystemService.getSystemHealth());
    }

    @GetMapping("/integration-status")
    @PreAuthorize("hasAuthority('SYSTEM_CONFIGURE')")
    public ApiResponse<List<IntegrationStatusResponse>> integrationStatus() {
        return ApiResponse.success(adminSystemService.getIntegrationStatus());
    }

    @GetMapping("/backup-status")
    @PreAuthorize("hasAuthority('SYSTEM_CONFIGURE')")
    public ApiResponse<BackupStatusResponse> backupStatus() {
        return ApiResponse.success(adminSystemService.getBackupStatus());
    }

    @GetMapping("/compliance")
    @PreAuthorize("hasAuthority('SYSTEM_CONFIGURE')")
    public ApiResponse<ComplianceDashboardResponse> compliance() {
        return ApiResponse.success(adminSystemService.getComplianceDashboard());
    }

    @PostMapping("/run-license-expiry-check")
    @PreAuthorize("hasAuthority('SYSTEM_CONFIGURE')")
    public ApiResponse<String> runLicenseExpiryCheck() {
        int count = licenseExpiryAlertJob.scan();
        return ApiResponse.success("License expiry scan completed", count + " doctor(s) alerted");
    }
}