package com.pharmacy.pipms.billing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InsuranceClaimRequest {

    @NotBlank(message = "Insurance provider is required")
    @Size(max = 150)
    private String insuranceProvider;

    @NotBlank(message = "Claim number is required")
    @Size(max = 100)
    private String insuranceClaimNumber;

    @NotNull(message = "Claimed amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Claimed amount must be positive")
    private BigDecimal claimedAmount;
}