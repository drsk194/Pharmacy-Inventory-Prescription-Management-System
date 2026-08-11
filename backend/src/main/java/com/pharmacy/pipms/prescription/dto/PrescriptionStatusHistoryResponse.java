package com.pharmacy.pipms.prescription.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PrescriptionStatusHistoryResponse {
    private String fromStatus;
    private String toStatus;
    private String changedByName;
    private String notes;
    private LocalDateTime timestamp;
}