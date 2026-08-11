package com.pharmacy.pipms.interaction.dto;

import com.pharmacy.pipms.interaction.entity.InteractionSeverity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrugInteractionCreateRequest {
    @NotNull(message = "First drug ID is required")
    private Long drugAId;

    @NotNull(message = "Second drug ID is required")
    private Long drugBId;

    @NotNull(message = "Severity is required")
    private InteractionSeverity severity;

    @Size(max = 500)
    private String description;
}