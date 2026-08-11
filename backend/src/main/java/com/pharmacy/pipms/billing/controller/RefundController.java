package com.pharmacy.pipms.billing.controller;

import com.pharmacy.pipms.billing.dto.BillResponse;
import com.pharmacy.pipms.billing.dto.RefundCreateRequest;
import com.pharmacy.pipms.billing.service.BillingService;
import com.pharmacy.pipms.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
@Tag(name = "Refunds", description = "Processing refunds against a bill")
public class RefundController {

    private final BillingService billingService;

    @PostMapping
    @PreAuthorize("hasAuthority('REFUND_PROCESS')")
    public ApiResponse<BillResponse> create(@Valid @RequestBody RefundCreateRequest request, Authentication authentication) {
        return ApiResponse.success("Refund processed", billingService.processRefund(request, authentication.getName()));
    }
}