package com.pharmacy.pipms.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class BillableDispensingResponse {
    private Long id;
    private String drugGenericName;
    private BigDecimal quantityDispensed;
    private String status;
}
