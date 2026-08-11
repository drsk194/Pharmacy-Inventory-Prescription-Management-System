package com.pharmacy.pipms.purchaseorder.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ReorderSuggestionResponse {
    private Long drugId;
    private String drugGenericName;
    private BigDecimal currentStock;
    private Integer reorderLevel;
    private Integer targetStockLevel;
    private BigDecimal suggestedOrderQuantity;
}