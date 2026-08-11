package com.pharmacy.pipms.dispensing.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.dispensing.dto.*;
import com.pharmacy.pipms.dispensing.entity.BalanceOrderStatus;
import com.pharmacy.pipms.dispensing.entity.DispensingStatus;
import com.pharmacy.pipms.dispensing.service.DispensingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dispensing")
@RequiredArgsConstructor
@Tag(name = "Dispensing", description = "Technician preparation, pharmacist authorization, labels, counselling, returns, and error reporting")
public class DispensingController {

    private final DispensingService dispensingService;

    @PostMapping("/prepare")
    @PreAuthorize("hasAuthority('DISPENSING_PREPARE')")
    public ApiResponse<DispensingRecordResponse> prepare(@Valid @RequestBody DispensingPrepareRequest request,
                                                           Authentication authentication) {
        return ApiResponse.success("Dispensing prepared", dispensingService.prepare(request, authentication.getName()));
    }

    @PutMapping("/{id}/authorize")
    @PreAuthorize("hasAuthority('DISPENSING_AUTHORIZE')")
    public ApiResponse<DispensingRecordResponse> authorize(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success("Dispensing authorized — inventory updated",
                dispensingService.authorize(id, authentication.getName()));
    }

    @PutMapping("/{id}/label")
    @PreAuthorize("hasAuthority('DISPENSING_PREPARE')")
    public ApiResponse<DispensingRecordResponse> printLabel(@PathVariable Long id) {
        return ApiResponse.success("Label printed", dispensingService.printLabel(id));
    }

    @GetMapping("/{id}/label")
    @PreAuthorize("hasAuthority('DISPENSING_PREPARE') or hasAuthority('REPORT_DISPENSING')")
    public ApiResponse<LabelResponse> getLabel(@PathVariable Long id) {
        return ApiResponse.success(dispensingService.getLabel(id));
    }

    @PutMapping("/{id}/acknowledge")
    @PreAuthorize("hasAuthority('DISPENSING_PREPARE')")
    public ApiResponse<DispensingRecordResponse> acknowledge(@PathVariable Long id,
                                                              @Valid @RequestBody AcknowledgeRequest request) {
        return ApiResponse.success("Patient acknowledgement recorded", dispensingService.acknowledge(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DISPENSING_PREPARE') or hasAuthority('REPORT_DISPENSING')")
    public ApiResponse<DispensingRecordResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(dispensingService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REPORT_DISPENSING')")
    public ApiResponse<PageResponse<DispensingRecordResponse>> search(
            @RequestParam(required = false) DispensingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(dispensingService.search(status, PageRequest.of(page, size)));
    }

    @PostMapping("/counselling")
    @PreAuthorize("hasRole('PHARMACIST') or hasRole('ADMIN')")
    public ApiResponse<CounsellingRecordResponse> addCounselling(@Valid @RequestBody CounsellingRequest request,
                                                                  Authentication authentication) {
        return ApiResponse.success("Counselling documented",
                dispensingService.addCounselling(request, authentication.getName()));
    }

    @GetMapping("/{id}/counselling")
    @PreAuthorize("hasAuthority('DISPENSING_PREPARE') or hasAuthority('REPORT_DISPENSING')")
    public ApiResponse<List<CounsellingRecordResponse>> getCounselling(@PathVariable Long id) {
        return ApiResponse.success(dispensingService.getCounselling(id));
    }

    @PostMapping("/returns")
    @PreAuthorize("hasAuthority('DISPENSING_PREPARE')")
    public ApiResponse<MedicationReturnResponse> processReturn(@Valid @RequestBody MedicationReturnRequest request,
                                                                Authentication authentication) {
        return ApiResponse.success("Return processed, inventory credited",
                dispensingService.processReturn(request, authentication.getName()));
    }

    @GetMapping("/returns")
    @PreAuthorize("hasAuthority('REPORT_DISPENSING')")
    public ApiResponse<List<MedicationReturnResponse>> getReturns() {
        return ApiResponse.success(dispensingService.getReturns());
    }

    @PostMapping("/errors")
    @PreAuthorize("hasAuthority('DISPENSING_PREPARE')")
    public ApiResponse<DispensingErrorResponse> reportError(@Valid @RequestBody DispensingErrorRequest request,
                                                             Authentication authentication) {
        return ApiResponse.success("Dispensing error reported",
                dispensingService.reportError(request, authentication.getName()));
    }

    @GetMapping("/errors")
    @PreAuthorize("hasAuthority('REPORT_DISPENSING')")
    public ApiResponse<List<DispensingErrorResponse>> getErrors() {
        return ApiResponse.success(dispensingService.getErrors());
    }

    @GetMapping("/balance-orders")
    @PreAuthorize("hasAuthority('REPORT_DISPENSING') or hasAuthority('PRESCRIPTION_READ_ALL')")
    public ApiResponse<List<BalanceOrderResponse>> getBalanceOrders(
            @RequestParam(required = false) BalanceOrderStatus status) {
        return ApiResponse.success(dispensingService.getBalanceOrders(status));
    }
}