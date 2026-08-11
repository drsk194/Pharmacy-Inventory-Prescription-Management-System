package com.pharmacy.pipms.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class ProcurementSpendingResponse {
    private BigDecimal totalSpending;
    private List<LabeledAmountResponse> bySupplier;
}