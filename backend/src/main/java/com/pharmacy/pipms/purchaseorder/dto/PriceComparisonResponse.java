package com.pharmacy.pipms.purchaseorder.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PriceComparisonResponse {
    private Long supplierId;
    private String supplierName;
    private BigDecimal averagePrice;
    private LocalDateTime mostRecentPurchaseDate;
}