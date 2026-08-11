package com.pharmacy.pipms.controlledsubstance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReauthenticateRequest {
    @NotBlank(message = "PIN is required")
    private String pin;
}