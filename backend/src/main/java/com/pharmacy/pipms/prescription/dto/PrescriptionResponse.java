package com.pharmacy.pipms.prescription.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PrescriptionResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private LocalDate prescriptionDate;
    private LocalDateTime receiptDate;
    private String source;
    private String status;
    private boolean controlled;
    private String verifyingPharmacistName;
    private String notes;
    private String rejectionReason;
    private List<PrescriptionItemResponse> items;
}