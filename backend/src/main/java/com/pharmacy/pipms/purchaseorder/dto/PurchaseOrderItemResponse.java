package com.pharmacy.pipms.purchaseorder.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PurchaseOrderItemResponse {
    private Long id;
    private Long drugId;
    private String drugGenericName;
    private BigDecimal orderedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal receivedQuantity;
    private String status;
}