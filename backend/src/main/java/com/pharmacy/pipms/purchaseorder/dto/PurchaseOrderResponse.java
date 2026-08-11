package com.pharmacy.pipms.purchaseorder.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PurchaseOrderResponse {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private String status;
    private BigDecimal totalValue;
    private String approvedByName;
    private LocalDateTime approvalDate;
    private String rejectionReason;
    private String deliveryTerms;
    private List<PurchaseOrderItemResponse> items;
}