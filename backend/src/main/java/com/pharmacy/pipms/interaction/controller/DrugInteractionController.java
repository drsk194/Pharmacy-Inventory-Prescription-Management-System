package com.pharmacy.pipms.interaction.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.interaction.dto.DrugInteractionCreateRequest;
import com.pharmacy.pipms.interaction.dto.DrugInteractionResponse;
import com.pharmacy.pipms.interaction.service.DrugInteractionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drug-interactions")
@RequiredArgsConstructor
@Tag(name = "Drug Interactions", description = "Master data for drug-drug interaction checking (used by prescription verification)")
public class DrugInteractionController {

    private final DrugInteractionService interactionService;

    @PostMapping
    @PreAuthorize("hasAuthority('SYSTEM_CONFIGURE')")
    public ApiResponse<DrugInteractionResponse> create(@Valid @RequestBody DrugInteractionCreateRequest request) {
        return ApiResponse.success("Interaction recorded", interactionService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DRUG_READ')")
    public ApiResponse<List<DrugInteractionResponse>> getAll() {
        return ApiResponse.success(interactionService.getAll());
    }
}