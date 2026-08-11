package com.pharmacy.pipms.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class DrugMovementResponse {
    private Long drugId;
    private String drugGenericName;
    private long dispensingEventCount;
    private BigDecimal totalQuantityDispensed;
}