package com.pharmacy.pipms.prescription.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PrescriptionCheckResponse {
    private PrescriptionResponse prescription;
    private List<VerificationWarningResponse> warnings;
}