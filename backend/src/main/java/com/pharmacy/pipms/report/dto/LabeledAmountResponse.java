package com.pharmacy.pipms.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class LabeledAmountResponse {
    private String label;
    private BigDecimal amount;
}