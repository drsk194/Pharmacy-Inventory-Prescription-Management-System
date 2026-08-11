package com.pharmacy.pipms.grn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SupplierPerformanceResponse {
    private Long supplierId;
    private String supplierName;
    private long totalGrnCount;
    private double onTimeDeliveryRatePercent;
    private double quantityFillRatePercent;
    private double qualityDiscrepancyRatePercent;
}