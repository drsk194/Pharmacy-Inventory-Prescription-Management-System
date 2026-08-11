package com.pharmacy.pipms.dispensing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcknowledgeRequest {
    @NotBlank(message = "Acknowledger's name is required")
    @Size(max = 150)
    private String acknowledgedByName;

    @NotBlank(message = "Relation is required")
    @Pattern(regexp = "^(SELF|CAREGIVER)$", message = "Relation must be SELF or CAREGIVER")
    private String acknowledgedRelation;
}