package com.pharmacy.pipms.dispensing.dto;

import com.pharmacy.pipms.dispensing.entity.CounsellingType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CounsellingRequest {
    @NotNull(message = "Dispensing record ID is required")
    private Long dispensingRecordId;

    @NotNull(message = "Counselling type is required")
    private CounsellingType counsellingType;

    @Size(max = 1000)
    private String notes;
}