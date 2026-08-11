package com.pharmacy.pipms.fefo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class BatchAllocationResult {
    private Long batchId;
    private String batchNumber;
    private LocalDate expiryDate;
    private BigDecimal quantityAllocated;
    private BigDecimal remainingInBatch;
}