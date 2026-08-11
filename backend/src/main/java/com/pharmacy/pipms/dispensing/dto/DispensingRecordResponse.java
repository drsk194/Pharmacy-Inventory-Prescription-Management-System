package com.pharmacy.pipms.dispensing.dto;

import com.pharmacy.pipms.fefo.dto.BatchAllocationResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class DispensingRecordResponse {
    private Long id;
    private Long prescriptionItemId;
    private String drugGenericName;
    private String technicianName;
    private String pharmacistName;
    private BigDecimal quantityIntended;
    private BigDecimal quantityDispensed;
    private String status;
    private boolean labelPrinted;
    private LocalDateTime dispensedAt;
    private boolean patientAcknowledged;
    private List<BatchAllocationResult> batchAllocations;
}