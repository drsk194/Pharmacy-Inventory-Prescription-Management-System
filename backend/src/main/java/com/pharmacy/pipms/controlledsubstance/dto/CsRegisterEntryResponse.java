package com.pharmacy.pipms.controlledsubstance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CsRegisterEntryResponse {
    private Long id;
    private Long drugId;
    private String drugGenericName;
    private String transactionType;
    private BigDecimal quantity;
    private BigDecimal balanceAfter;
    private Long prescriptionId;
    private String technicianName;
    private String pharmacistName;
    private String witnessName;
    private LocalDateTime transactionDate;
    private String notes;
    private String entryHash;
}