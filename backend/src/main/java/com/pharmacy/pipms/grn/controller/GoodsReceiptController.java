package com.pharmacy.pipms.grn.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.grn.dto.*;
import com.pharmacy.pipms.grn.service.GoodsReceiptService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grn")
@RequiredArgsConstructor
@Tag(name = "Goods Receipt Notes", description = "Receiving stock against purchase orders — automatic batch creation and inventory increase")
public class GoodsReceiptController {

    private final GoodsReceiptService goodsReceiptService;

    @PostMapping
    @PreAuthorize("hasAuthority('GRN_CREATE')")
    public ApiResponse<GoodsReceiptNoteResponse> create(@Valid @RequestBody GoodsReceiptNoteCreateRequest request,
                                                          Authentication authentication) {
        return ApiResponse.success("Goods receipt processed — batches created and inventory updated",
                goodsReceiptService.create(request, authentication));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GRN_CREATE') or hasAuthority('REPORT_PROCUREMENT')")
    public ApiResponse<GoodsReceiptNoteResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(goodsReceiptService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('GRN_CREATE') or hasAuthority('REPORT_PROCUREMENT')")
    public ApiResponse<PageResponse<GoodsReceiptNoteResponse>> search(
            @RequestParam(required = false) Long purchaseOrderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(goodsReceiptService.search(purchaseOrderId, PageRequest.of(page, size)));
    }

    @GetMapping("/discrepancies")
    @PreAuthorize("hasAuthority('GRN_CREATE') or hasAuthority('REPORT_PROCUREMENT')")
    public ApiResponse<List<GoodsReceiptItemResponse>> discrepancies() {
        return ApiResponse.success(goodsReceiptService.getDiscrepancies());
    }

    @GetMapping("/supplier-performance/{supplierId}")
    @PreAuthorize("hasAuthority('GRN_CREATE') or hasAuthority('REPORT_PROCUREMENT')")
    public ApiResponse<SupplierPerformanceResponse> supplierPerformance(@PathVariable Long supplierId) {
        return ApiResponse.success(goodsReceiptService.getSupplierPerformance(supplierId));
    }
}