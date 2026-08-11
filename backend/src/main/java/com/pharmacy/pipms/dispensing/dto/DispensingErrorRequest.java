package com.pharmacy.pipms.dispensing.dto;

import com.pharmacy.pipms.dispensing.entity.DispensingErrorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DispensingErrorRequest {
    private Long dispensingRecordId; // nullable — see DispensingError entity note

    @NotNull(message = "Error type is required")
    private DispensingErrorType errorType;

    @NotBlank(message = "Description is required")
    @Size(max = 1000)
    private String description;

    @Size(max = 1000)
    private String correctiveAction;
}