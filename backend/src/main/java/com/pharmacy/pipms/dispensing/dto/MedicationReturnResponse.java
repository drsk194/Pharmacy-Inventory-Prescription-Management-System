package com.pharmacy.pipms.dispensing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MedicationReturnResponse {
    private Long id;
    private Long dispensingRecordId;
    private Long batchId;
    private String batchNumber;
    private BigDecimal quantityReturned;
    private String reason;
    private String processedByName;
    private LocalDateTime createdAt;
}