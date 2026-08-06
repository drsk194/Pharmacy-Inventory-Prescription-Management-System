package com.pharmacy.pipms.inventory.controller;

import com.pharmacy.pipms.batch.service.BatchService;
import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.inventory.dto.AdjustmentCreateRequest;
import com.pharmacy.pipms.inventory.dto.AdjustmentResponse;
import com.pharmacy.pipms.inventory.dto.StockCountRequest;
import com.pharmacy.pipms.inventory.entity.AdjustmentStatus;
import com.pharmacy.pipms.inventory.service.InventoryAdjustmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Adjustments", description = "Stock adjustments, physical counts, approvals, and variance reports")
public class InventoryAdjustmentController {

    private final InventoryAdjustmentService adjustmentService;
    private final BatchService batchService;

    @PostMapping("/adjustments")
    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
    public ApiResponse<AdjustmentResponse> createAdjustment(@Valid @RequestBody AdjustmentCreateRequest request,
                                                              Authentication authentication) {
        return ApiResponse.success("Adjustment recorded",
                adjustmentService.createAdjustment(request, authentication.getName()));
    }

    @PostMapping("/stock-count")
    @PreAuthorize("hasAuthority('INVENTORY_COUNT')")
    public ApiResponse<AdjustmentResponse> stockCount(@Valid @RequestBody StockCountRequest request,
                                                        Authentication authentication) {
        return ApiResponse.success("Stock count recorded",
                adjustmentService.createStockCount(request, authentication.getName()));
    }

    @GetMapping("/adjustments")
    @PreAuthorize("hasAuthority('INVENTORY_ADJUST') or hasAuthority('REPORT_INVENTORY')")
    public ApiResponse<PageResponse<AdjustmentResponse>> search(
            @RequestParam(required = false) AdjustmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(adjustmentService.search(status, pageable));
    }

    @GetMapping("/adjustments/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_ADJUST') or hasAuthority('REPORT_INVENTORY')")
    public ApiResponse<AdjustmentResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(adjustmentService.getById(id));
    }

    // Supervisor approval — gated by role since the SRS has no dedicated
    // "approve" permission. See Assumption 6 in this module's notes.
    @PatchMapping("/adjustments/{id}/approve")
    @PreAuthorize("hasRole('PHARMACIST') or hasRole('ADMIN')")
    public ApiResponse<AdjustmentResponse> approve(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success("Adjustment approved",
                adjustmentService.decide(id, true, authentication.getName()));
    }

    @PatchMapping("/adjustments/{id}/reject")
    @PreAuthorize("hasRole('PHARMACIST') or hasRole('ADMIN')")
    public ApiResponse<AdjustmentResponse> reject(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success("Adjustment rejected",
                adjustmentService.decide(id, false, authentication.getName()));
    }

    @GetMapping("/variance-report")
    @PreAuthorize("hasAuthority('REPORT_INVENTORY')")
    public ApiResponse<List<AdjustmentResponse>> varianceReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate + "T00:00:00")
                : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate + "T23:59:59")
                : LocalDateTime.now();
        return ApiResponse.success(adjustmentService.varianceReport(start, end));
    }

    // Testing convenience — mirrors BatchAutoQuarantineJob's daily
    // scheduled run, so this can be verified on demand instead of waiting
    // for the cron trigger. See Assumption 8 in this module's notes.
    @PostMapping("/run-expiry-check")
    @PreAuthorize("hasAuthority('SYSTEM_CONFIGURE')")
    public ApiResponse<String> runExpiryCheck() {
        int affected = batchService.runExpiryCheck();
        return ApiResponse.success("Expiry check completed", affected + " batch(es) updated");
    }
}