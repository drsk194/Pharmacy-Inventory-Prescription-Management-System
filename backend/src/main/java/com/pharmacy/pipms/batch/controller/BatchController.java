package com.pharmacy.pipms.batch.controller;

import com.pharmacy.pipms.batch.dto.BatchCreateRequest;
import com.pharmacy.pipms.batch.dto.BatchResponse;
import com.pharmacy.pipms.batch.entity.BatchStatus;
import com.pharmacy.pipms.batch.service.BatchService;
import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.inventory.dto.StockMovementResponse;
import com.pharmacy.pipms.inventory.service.StockMovementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
@Tag(name = "Batches", description = "Drug batch creation, tracking, and expiry/quarantine management")
public class BatchController {

    private final BatchService batchService;
    private final StockMovementService stockMovementService;

    @PostMapping
    @PreAuthorize("hasAuthority('BATCH_CREATE')")
    public ApiResponse<BatchResponse> createBatch(@Valid @RequestBody BatchCreateRequest request,
                                                    Authentication authentication) {
        return ApiResponse.success("Batch created", batchService.createBatch(request, authentication));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BATCH_READ')")
    public ApiResponse<BatchResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(batchService.getById(id));
    }
    @GetMapping("/by-drug/{drugId}")
    @PreAuthorize("hasAuthority('BATCH_READ')")
    public ApiResponse<List<BatchResponse>> getByDrug(@PathVariable Long drugId) {
        return ApiResponse.success(batchService.getByDrug(drugId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('BATCH_READ')")
    public ApiResponse<PageResponse<BatchResponse>> search(
            @RequestParam(required = false) BatchStatus status,
            @RequestParam(required = false) Long locationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(batchService.search(status, locationId, pageable));
    }

    @GetMapping("/expired")
    @PreAuthorize("hasAuthority('BATCH_READ')")
    public ApiResponse<PageResponse<BatchResponse>> getExpired(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(batchService.getExpired(PageRequest.of(page, size)));
    }

    @GetMapping("/quarantined")
    @PreAuthorize("hasAuthority('BATCH_READ')")
    public ApiResponse<PageResponse<BatchResponse>> getQuarantined(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(batchService.getQuarantined(PageRequest.of(page, size)));
    }

    @PatchMapping("/{id}/quarantine")
    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
    public ApiResponse<BatchResponse> quarantine(@PathVariable Long id, @RequestParam String reason,
                                                   Authentication authentication) {
        return ApiResponse.success("Batch quarantined", batchService.quarantine(id, reason, authentication));
    }

    @GetMapping("/{id}/movements")
    @PreAuthorize("hasAuthority('BATCH_READ')")
    public ApiResponse<List<StockMovementResponse>> getMovements(@PathVariable Long id) {
        return ApiResponse.success(stockMovementService.getHistory(id));
    }
}