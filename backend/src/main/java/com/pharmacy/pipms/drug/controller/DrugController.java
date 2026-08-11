package com.pharmacy.pipms.drug.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.drug.dto.*;
import com.pharmacy.pipms.drug.entity.DrugSchedule;
import com.pharmacy.pipms.drug.service.DrugService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drugs")
@RequiredArgsConstructor
@Tag(name = "Drugs", description = "Drug master catalog — creation, updates, and public guest browsing")
public class DrugController {

    private final DrugService drugService;
    private final com.pharmacy.pipms.batch.service.BatchService batchService;
    private final com.pharmacy.pipms.drug.repository.DrugRepository drugRepository;

    @PostMapping
    @PreAuthorize("hasAuthority('DRUG_CREATE')")
    public ApiResponse<DrugResponse> createDrug(@Valid @RequestBody DrugCreateRequest request) {
        return ApiResponse.success("Drug created", drugService.createDrug(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DRUG_READ')")
    public ApiResponse<PageResponse<DrugResponse>> searchDrugs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String drugClass,
            @RequestParam(required = false) DrugSchedule schedule,
            @RequestParam(defaultValue = "false") boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(drugService.searchDrugs(search, drugClass, schedule, activeOnly, pageable));
    }

    @GetMapping("/catalog")
    public ApiResponse<PageResponse<DrugCatalogResponse>> publicCatalog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(drugService.getPublicCatalog(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DRUG_READ')")
    public ApiResponse<DrugResponse> getDrug(@PathVariable Long id) {
        return ApiResponse.success(drugService.getDrugById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DRUG_UPDATE')")
    public ApiResponse<DrugResponse> updateDrug(@PathVariable Long id, @Valid @RequestBody DrugUpdateRequest request) {
        return ApiResponse.success("Drug updated", drugService.updateDrug(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('DRUG_DEACTIVATE')")
    public ApiResponse<DrugResponse> setStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponse.success(
                active ? "Drug reactivated" : "Drug deactivated",
                drugService.setActive(id, active));
    }
@GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('DRUG_READ')")
    public ApiResponse<java.util.List<DrugResponse>> lowStock() {
        java.util.List<DrugResponse> result = drugRepository.findAll().stream()
                .filter(com.pharmacy.pipms.drug.entity.Drug::isActive)
                .filter(batchService::isLowStock)
                .map(d -> drugService.getDrugById(d.getId()))
                .collect(java.util.stream.Collectors.toList());
        return ApiResponse.success(result);
    }

    @GetMapping("/near-expiry")
    @PreAuthorize("hasAuthority('DRUG_READ')")
    public ApiResponse<java.util.List<DrugResponse>> nearExpiry(@RequestParam(defaultValue = "90") int days) {
        java.util.List<Long> drugIds = batchService.findExpiringDrugIds(days);
        java.util.List<DrugResponse> result = drugIds.stream()
                .map(drugService::getDrugById)
                .collect(java.util.stream.Collectors.toList());
        return ApiResponse.success(result);
    }
}