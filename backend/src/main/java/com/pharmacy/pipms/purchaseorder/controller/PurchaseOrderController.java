package com.pharmacy.pipms.purchaseorder.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.purchaseorder.dto.*;
import com.pharmacy.pipms.purchaseorder.entity.PurchaseOrderStatus;
import com.pharmacy.pipms.purchaseorder.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@Tag(name = "Purchase Orders", description = "Draft creation, line items, value-threshold approval workflow, and reorder suggestions")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_CREATE')")
    public ApiResponse<PurchaseOrderResponse> create(@Valid @RequestBody PurchaseOrderCreateRequest request) {
        return ApiResponse.success("Purchase order drafted", purchaseOrderService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_CREATE') or hasAuthority('REPORT_PROCUREMENT')")
    public ApiResponse<PageResponse<PurchaseOrderResponse>> search(
            @RequestParam(required = false) PurchaseOrderStatus status,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(purchaseOrderService.search(status, supplierId, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_CREATE') or hasAuthority('REPORT_PROCUREMENT')")
    public ApiResponse<PurchaseOrderResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(purchaseOrderService.getById(id));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_CREATE')")
    public ApiResponse<PurchaseOrderResponse> addItem(@PathVariable Long id,
                                                        @Valid @RequestBody PurchaseOrderItemRequest request) {
        return ApiResponse.success("Item added", purchaseOrderService.addItem(id, request));
    }

    @PutMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_CREATE')")
    public ApiResponse<PurchaseOrderResponse> updateItem(@PathVariable Long id, @PathVariable Long itemId,
                                                          @Valid @RequestBody PurchaseOrderItemRequest request) {
        return ApiResponse.success("Item updated", purchaseOrderService.updateItem(id, itemId, request));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_CREATE')")
    public ApiResponse<PurchaseOrderResponse> removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        return ApiResponse.success("Item removed", purchaseOrderService.removeItem(id, itemId));
    }

    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_CREATE')")
    public ApiResponse<PurchaseOrderResponse> submit(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success("Purchase order submitted", purchaseOrderService.submit(id, authentication.getName()));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_APPROVE')")
    public ApiResponse<PurchaseOrderResponse> approve(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success("Purchase order approved", purchaseOrderService.approve(id, authentication.getName()));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_APPROVE')")
    public ApiResponse<PurchaseOrderResponse> reject(@PathVariable Long id,
                                                       @Valid @RequestBody RejectPurchaseOrderRequest request) {
        return ApiResponse.success("Purchase order rejected", purchaseOrderService.reject(id, request));
    }

    @GetMapping("/reorder-suggestions")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_CREATE') or hasAuthority('REPORT_PROCUREMENT')")
    public ApiResponse<List<ReorderSuggestionResponse>> reorderSuggestions() {
        return ApiResponse.success(purchaseOrderService.getReorderSuggestions());
    }

    @GetMapping("/price-comparison")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_CREATE') or hasAuthority('REPORT_PROCUREMENT')")
    public ApiResponse<List<PriceComparisonResponse>> priceComparison(@RequestParam Long drugId) {
        return ApiResponse.success(purchaseOrderService.getPriceComparison(drugId));
    }
}