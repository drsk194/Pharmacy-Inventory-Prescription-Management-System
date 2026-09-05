package com.pharmacy.pipms.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminSetControlledSubstancePinRequest {
    @NotBlank(message = "New PIN is required")
    @Pattern(regexp = "^\\d{4,6}$", message = "PIN must be 4-6 digits")
    private String newPin;
}
