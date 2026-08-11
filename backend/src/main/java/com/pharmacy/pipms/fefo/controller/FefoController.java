package com.pharmacy.pipms.fefo.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.exception.UserNotFoundException;
import com.pharmacy.pipms.fefo.dto.*;
import com.pharmacy.pipms.fefo.service.FefoAllocationService;
import com.pharmacy.pipms.user.entity.User;
import com.pharmacy.pipms.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fefo")
@RequiredArgsConstructor
@Tag(name = "FEFO", description = "First-Expiry-First-Out batch allocation. Module 11 will call this " +
        "service directly for real dispensing; these endpoints exist for standalone testing.")
public class FefoController {

    private final FefoAllocationService fefoAllocationService;
    private final UserRepository userRepository;

    @PostMapping("/plan")
    @PreAuthorize("hasAuthority('BATCH_READ')")
    public ApiResponse<FefoPlanResponse> plan(@Valid @RequestBody FefoPlanRequest request) {
        return ApiResponse.success(fefoAllocationService.planAllocation(request.getDrugId(), request.getQuantity()));
    }

    @PostMapping("/consume")
    @PreAuthorize("hasAuthority('DISPENSING_PREPARE')")
    public ApiResponse<FefoPlanResponse> consume(@Valid @RequestBody FefoConsumeRequest request,
                                                  Authentication authentication) {
        User user = currentUser(authentication);
        String refType = request.getReferenceType() != null ? request.getReferenceType() : "MANUAL_TEST";
        FefoPlanResponse result = fefoAllocationService.consume(
                request.getDrugId(), request.getQuantity(), refType, request.getReferenceId(), user);
        return ApiResponse.success("Stock allocated via FEFO", result);
    }

    @PostMapping("/override")
    @PreAuthorize("hasAuthority('DISPENSING_AUTHORIZE')")
    public ApiResponse<FefoPlanResponse> override(@Valid @RequestBody FefoOverrideRequest request,
                                                   Authentication authentication) {
        User user = currentUser(authentication);
        String refType = request.getReferenceType() != null ? request.getReferenceType() : "MANUAL_TEST";
        FefoPlanResponse result = fefoAllocationService.consumeWithOverride(
                request.getBatchId(), request.getQuantity(), request.getReason(),
                refType, request.getReferenceId(), user);
        return ApiResponse.success("Batch dispensed via pharmacist override", result);
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}