package com.pharmacy.pipms.billing.controller;

import com.pharmacy.pipms.billing.dto.BillResponse;
import com.pharmacy.pipms.billing.dto.PaymentCreateRequest;
import com.pharmacy.pipms.billing.service.BillingService;
import com.pharmacy.pipms.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Recording cash/card/UPI payments against a bill")
public class PaymentController {

    private final BillingService billingService;

    @PostMapping
    @PreAuthorize("hasAuthority('PAYMENT_PROCESS')")
    public ApiResponse<BillResponse> create(@Valid @RequestBody PaymentCreateRequest request, Authentication authentication) {
        return ApiResponse.success("Payment recorded", billingService.recordPayment(request, authentication.getName()));
    }
}