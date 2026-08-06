package com.pharmacy.pipms.inventory.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.inventory.dto.LocationCreateRequest;
import com.pharmacy.pipms.inventory.dto.LocationResponse;
import com.pharmacy.pipms.inventory.service.InventoryLocationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/locations")
@RequiredArgsConstructor
@Tag(name = "Inventory Locations", description = "Main pharmacy, satellite, and ward stock locations")
public class InventoryLocationController {

    private final InventoryLocationService locationService;

    @PostMapping
    @PreAuthorize("hasAuthority('SYSTEM_CONFIGURE')")
    public ApiResponse<LocationResponse> create(@Valid @RequestBody LocationCreateRequest request) {
        return ApiResponse.success("Location created", locationService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('BATCH_READ')")
    public ApiResponse<List<LocationResponse>> getAll() {
        return ApiResponse.success(locationService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BATCH_READ')")
    public ApiResponse<LocationResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(locationService.getById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('SYSTEM_CONFIGURE')")
    public ApiResponse<LocationResponse> setStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponse.success(
                active ? "Location reactivated" : "Location deactivated",
                locationService.setActive(id, active));
    }
}