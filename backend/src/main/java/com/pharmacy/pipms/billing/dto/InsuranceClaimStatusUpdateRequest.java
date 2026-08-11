package com.pharmacy.pipms.billing.dto;

import com.pharmacy.pipms.billing.entity.InsuranceClaimStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InsuranceClaimStatusUpdateRequest {
    @NotNull(message = "New status is required")
    private InsuranceClaimStatus status;
}