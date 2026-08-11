package com.pharmacy.pipms.prescription.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PrescriptionItemResponse {
    private Long id;
    private Long drugId;
    private String drugGenericName;
    private BigDecimal prescribedQuantity;
    private BigDecimal dispensedQuantity;
    private String dosage;
    private String frequency;
    private String duration;
    private String instructions;
}