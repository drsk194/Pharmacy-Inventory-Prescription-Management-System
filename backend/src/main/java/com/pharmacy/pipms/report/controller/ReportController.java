package com.pharmacy.pipms.report.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.common.CsvExportUtil;
import com.pharmacy.pipms.report.dto.*;
import com.pharmacy.pipms.report.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Inventory, dispensing, procurement, financial, and audit analytics")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/inventory-summary")
    @PreAuthorize("hasAuthority('REPORT_INVENTORY')")
    public ApiResponse<InventorySummaryResponse> inventorySummary() {
        return ApiResponse.success(reportService.getInventorySummary());
    }

    @GetMapping("/dead-stock")
    @PreAuthorize("hasAuthority('REPORT_INVENTORY')")
    public ApiResponse<List<DrugMovementResponse>> deadStock() {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(30); // arbitrary lookback window using reporting.dead-stock-lookback-days conceptually
        return ApiResponse.success(reportService.getDeadStock(start, end));
    }

    @GetMapping("/slow-moving")
    @PreAuthorize("hasAuthority('REPORT_INVENTORY')")
    public ApiResponse<List<DrugMovementResponse>> slowMoving(@RequestParam(required = false) String startDate,
                                                                @RequestParam(required = false) String endDate) {
        LocalDateTime[] range = resolveRange(startDate, endDate);
        return ApiResponse.success(reportService.getSlowMovingStock(range[0], range[1]));
    }

    @GetMapping("/stock-turnover")
    @PreAuthorize("hasAuthority('REPORT_INVENTORY')")
    public ApiResponse<List<StockTurnoverResponse>> stockTurnover(@RequestParam(required = false) String startDate,
                                                                    @RequestParam(required = false) String endDate) {
        LocalDateTime[] range = resolveRange(startDate, endDate);
        return ApiResponse.success(reportService.getStockTurnover(range[0], range[1]));
    }

    // Reuses Module 8/6's already-built low-stock/near-expiry/expired
    // endpoints (GET /api/drugs/low-stock, /near-expiry, /api/batches/expired)
    // rather than duplicating them here — see this module's assumptions.

    // ---------- Prescription / Dispensing ----------

    @GetMapping("/prescription-volume")
    @PreAuthorize("hasAuthority('REPORT_DISPENSING')")
    public ApiResponse<PrescriptionVolumeResponse> prescriptionVolume(
            @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate) {
        LocalDateTime[] range = resolveRange(startDate, endDate);
        return ApiResponse.success(reportService.getPrescriptionVolume(range[0], range[1]));
    }

    @GetMapping("/prescription-volume/export")
    @PreAuthorize("hasAuthority('REPORT_DISPENSING')")
    public ResponseEntity<byte[]> prescriptionVolumeCsv(@RequestParam(required = false) String startDate,
                                                          @RequestParam(required = false) String endDate) {
        LocalDateTime[] range = resolveRange(startDate, endDate);
        PrescriptionVolumeResponse data = reportService.getPrescriptionVolume(range[0], range[1]);
        List<String[]> rows = data.getByStatus().stream()
                .map(l -> new String[]{"Status", l.getLabel(), String.valueOf(l.getCount())})
                .collect(java.util.stream.Collectors.toList());
        return CsvExportUtil.toCsvResponse("prescription-volume.csv", new String[]{"Dimension", "Label", "Count"}, rows);
    }

    @GetMapping("/dispensing-turnaround")
    @PreAuthorize("hasAuthority('REPORT_DISPENSING')")
    public ApiResponse<DispensingTurnaroundResponse> dispensingTurnaround(
            @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate) {
        LocalDateTime[] range = resolveRange(startDate, endDate);
        return ApiResponse.success(reportService.getDispensingTurnaround(range[0], range[1]));
    }

    @GetMapping("/technician-activity")
    @PreAuthorize("hasAuthority('REPORT_DISPENSING')")
    public ApiResponse<List<LabeledCountResponse>> technicianActivity(
            @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate) {
        LocalDateTime[] range = resolveRange(startDate, endDate);
        return ApiResponse.success(reportService.getTechnicianActivity(range[0], range[1]));
    }

    @GetMapping("/pharmacist-activity")
    @PreAuthorize("hasAuthority('REPORT_DISPENSING')")
    public ApiResponse<List<LabeledCountResponse>> pharmacistActivity(
            @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate) {
        LocalDateTime[] range = resolveRange(startDate, endDate);
        return ApiResponse.success(reportService.getPharmacistActivity(range[0], range[1]));
    }

    @GetMapping("/drug-utilization")
    @PreAuthorize("hasAuthority('REPORT_DISPENSING')")
    public ApiResponse<List<DrugMovementResponse>> drugUtilization(
            @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate) {
        LocalDateTime[] range = resolveRange(startDate, endDate);
        return ApiResponse.success(reportService.getDrugUtilization(range[0], range[1]));
    }

    // ---------- Procurement ----------
    // Controlled-substance reports already live at
    // GET /api/controlled-substances/reports (Module 12) — not duplicated here.
    // Single-supplier performance already lives at
    // GET /api/grn/supplier-performance/{id} (Module 14).

    @GetMapping("/procurement-spending")
    @PreAuthorize("hasAuthority('REPORT_PROCUREMENT')")
    public ApiResponse<ProcurementSpendingResponse> procurementSpending(
            @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        return ApiResponse.success(reportService.getProcurementSpending(start, end));
    }

    @GetMapping("/procurement-spending/export")
    @PreAuthorize("hasAuthority('REPORT_PROCUREMENT')")
    public ResponseEntity<byte[]> procurementSpendingCsv(@RequestParam(required = false) String startDate,
                                                          @RequestParam(required = false) String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        ProcurementSpendingResponse data = reportService.getProcurementSpending(start, end);
        List<String[]> rows = data.getBySupplier().stream()
                .map(l -> new String[]{l.getLabel(), l.getAmount().toPlainString()})
                .collect(java.util.stream.Collectors.toList());
        return CsvExportUtil.toCsvResponse("procurement-spending.csv", new String[]{"Supplier", "Total Spent"}, rows);
    }

    // ---------- Financial ----------

    @GetMapping("/revenue")
    @PreAuthorize("hasAuthority('REPORT_FINANCIAL')")
    public ApiResponse<RevenueSummaryResponse> revenue(@RequestParam(required = false) String startDate,
                                                         @RequestParam(required = false) String endDate) {
        LocalDateTime[] range = resolveRange(startDate, endDate);
        return ApiResponse.success(reportService.getRevenue(range[0], range[1]));
    }

    @GetMapping("/revenue/export")
    @PreAuthorize("hasAuthority('REPORT_FINANCIAL')")
    public ResponseEntity<byte[]> revenueCsv(@RequestParam(required = false) String startDate,
                                              @RequestParam(required = false) String endDate) {
        LocalDateTime[] range = resolveRange(startDate, endDate);
        RevenueSummaryResponse data = reportService.getRevenue(range[0], range[1]);
        List<String[]> rows = data.getDailyBreakdown().stream()
                .map(l -> new String[]{l.getLabel(), l.getAmount().toPlainString()})
                .collect(java.util.stream.Collectors.toList());
        return CsvExportUtil.toCsvResponse("revenue.csv", new String[]{"Date", "Revenue"}, rows);
    }

    @GetMapping("/outstanding")
    @PreAuthorize("hasAuthority('REPORT_FINANCIAL')")
    public ApiResponse<OutstandingSummaryResponse> outstanding() {
        return ApiResponse.success(reportService.getOutstanding());
    }

    // ---------- Audit ----------

    @GetMapping("/audit-activity")
    @PreAuthorize("hasAuthority('AUDIT_LOG_READ')")
    public ApiResponse<List<LabeledCountResponse>> auditActivity(@RequestParam(required = false) String startDate,
                                                                   @RequestParam(required = false) String endDate) {
        LocalDateTime[] range = resolveRange(startDate, endDate);
        return ApiResponse.success(reportService.getAuditActivity(range[0], range[1]));
    }

    // ---------- PDF structure (any report) ----------

    @GetMapping("/{reportName}/pdf-structure")
    @PreAuthorize("hasAuthority('REPORT_INVENTORY') or hasAuthority('REPORT_DISPENSING') " +
            "or hasAuthority('REPORT_PROCUREMENT') or hasAuthority('REPORT_FINANCIAL') or hasAuthority('AUDIT_LOG_READ')")
    public ApiResponse<PdfExportStructureResponse> pdfStructure(@PathVariable String reportName,
                                                                  @RequestParam(required = false) String startDate,
                                                                  @RequestParam(required = false) String endDate,
                                                                  Authentication authentication) {
        return ApiResponse.success(new PdfExportStructureResponse(
                reportName, authentication.getName(), LocalDateTime.now(),
                "startDate=" + startDate + ", endDate=" + endDate, 0,
                "PDF binary rendering not implemented — this endpoint returns the structure a template would use. " +
                        "See Module 17's Assumption 7."));
    }

    private LocalDateTime[] resolveRange(String startDate, String endDate) {
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate + "T00:00:00")
                : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate + "T23:59:59") : LocalDateTime.now();
        return new LocalDateTime[]{start, end};
    }
}