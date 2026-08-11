package com.pharmacy.pipms.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class BillItemResponse {
    private Long id;
    private Long dispensingRecordId;
    private String drugGenericName;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}