package com.pharmacy.pipms.supplier.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.supplier.dto.SupplierCreateRequest;
import com.pharmacy.pipms.supplier.dto.SupplierResponse;
import com.pharmacy.pipms.supplier.dto.SupplierUpdateRequest;
import com.pharmacy.pipms.supplier.service.SupplierService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Supplier master records, approval, and status management")
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @PreAuthorize("hasAuthority('SUPPLIER_MANAGE')")
    public ApiResponse<SupplierResponse> createSupplier(@Valid @RequestBody SupplierCreateRequest request) {
        return ApiResponse.success("Supplier created", supplierService.createSupplier(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUPPLIER_READ') or hasAuthority('SUPPLIER_MANAGE')")
    public ApiResponse<PageResponse<SupplierResponse>> searchSuppliers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean approvedOnly,
            @RequestParam(defaultValue = "false") boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(supplierService.searchSuppliers(search, approvedOnly, activeOnly, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_READ') or hasAuthority('SUPPLIER_MANAGE')")
    public ApiResponse<SupplierResponse> getSupplier(@PathVariable Long id) {
        return ApiResponse.success(supplierService.getSupplierById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_MANAGE')")
    public ApiResponse<SupplierResponse> updateSupplier(@PathVariable Long id,
                                                          @Valid @RequestBody SupplierUpdateRequest request) {
        return ApiResponse.success("Supplier updated", supplierService.updateSupplier(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('SUPPLIER_MANAGE')")
    public ApiResponse<SupplierResponse> setStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponse.success(
                active ? "Supplier reactivated" : "Supplier deactivated",
                supplierService.setActive(id, active));
    }

    @PatchMapping("/{id}/approval")
    @PreAuthorize("hasAuthority('SUPPLIER_MANAGE')")
    public ApiResponse<SupplierResponse> setApproval(@PathVariable Long id, @RequestParam boolean approved) {
        return ApiResponse.success(
                approved ? "Supplier approved" : "Supplier approval revoked",
                supplierService.setApproved(id, approved));
    }
}