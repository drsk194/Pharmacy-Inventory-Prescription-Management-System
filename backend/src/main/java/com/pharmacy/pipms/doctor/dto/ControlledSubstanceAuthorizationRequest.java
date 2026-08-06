package com.pharmacy.pipms.doctor.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ControlledSubstanceAuthorizationRequest {

    @NotNull(message = "Authorization flag is required")
    private Boolean authorized;

    // Required by the service layer whenever authorized = true
    private String authNumber;
}