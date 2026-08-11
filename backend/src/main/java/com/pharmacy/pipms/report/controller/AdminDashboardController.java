package com.pharmacy.pipms.report.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.prescription.service.PrescriptionService;
import com.pharmacy.pipms.report.dto.AdminAnalyticsResponse;
import com.pharmacy.pipms.report.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Live analytics summary and the report directory index")
public class AdminDashboardController {

    private final ReportService reportService;
    private final PrescriptionService prescriptionService;

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminAnalyticsResponse> analytics() {
        long queueCount = prescriptionService.getQueue().size();
        return ApiResponse.success(reportService.getAdminAnalytics(queueCount));
    }

    // Literal Appendix H path — satisfied as a directory index pointing to
    // the real, individually-parameterized endpoints. See Module 17's
    // "path conflict, resolved" note.
    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, String>> reportsIndex() {
        Map<String, String> index = new LinkedHashMap<>();
        index.put("inventorySummary", "/api/reports/inventory-summary");
        index.put("deadStock", "/api/reports/dead-stock");
        index.put("slowMoving", "/api/reports/slow-moving");
        index.put("stockTurnover", "/api/reports/stock-turnover");
        index.put("lowStock", "/api/drugs/low-stock");
        index.put("nearExpiry", "/api/drugs/near-expiry");
        index.put("expiredStock", "/api/batches/expired");
        index.put("prescriptionVolume", "/api/reports/prescription-volume");
        index.put("dispensingTurnaround", "/api/reports/dispensing-turnaround");
        index.put("technicianActivity", "/api/reports/technician-activity");
        index.put("pharmacistActivity", "/api/reports/pharmacist-activity");
        index.put("drugUtilization", "/api/reports/drug-utilization");
        index.put("controlledSubstances", "/api/controlled-substances/reports");
        index.put("supplierPerformance", "/api/grn/supplier-performance/{supplierId}");
        index.put("procurementSpending", "/api/reports/procurement-spending");
        index.put("revenue", "/api/reports/revenue");
        index.put("outstanding", "/api/reports/outstanding");
        index.put("auditActivity", "/api/reports/audit-activity");
        return ApiResponse.success(index);
    }
}