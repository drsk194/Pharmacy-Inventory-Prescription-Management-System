package com.pharmacy.pipms.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class InventorySummaryResponse {
    private long totalDrugs;
    private long activeBatchCount;
    private long nearExpiryBatchCount;
    private long expiredBatchCount;
    private long quarantinedBatchCount;
    private long exhaustedBatchCount;
    private BigDecimal totalStockValue;
    private long lowStockDrugCount;
}