package com.pharmacy.pipms.fefo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class FefoPlanResponse {
    private Long drugId;
    private String drugGenericName;
    private BigDecimal quantityRequested;
    private List<BatchAllocationResult> allocations;
}