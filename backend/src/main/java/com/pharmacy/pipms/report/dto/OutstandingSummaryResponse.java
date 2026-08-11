package com.pharmacy.pipms.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OutstandingSummaryResponse {
    private BigDecimal totalOutstanding;
    private long unpaidOrPartialBillCount;
}