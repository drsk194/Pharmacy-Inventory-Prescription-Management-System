package com.pharmacy.pipms.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ColdChainBreachRequest {

    @NotNull(message = "Drug ID is required")
    private Long drugId;

    private Long locationId; // optional

    @NotBlank(message = "A description of the breach is required")
    private String description;
}