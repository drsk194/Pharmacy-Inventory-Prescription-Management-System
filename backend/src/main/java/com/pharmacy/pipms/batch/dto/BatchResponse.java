package com.pharmacy.pipms.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class BatchResponse {
    private Long id;
    private Long drugId;
    private String drugGenericName;
    private String batchNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private Long supplierId;
    private String supplierName;
    private BigDecimal quantityReceived;
    private BigDecimal currentQuantity;
    private BigDecimal purchasePrice;
    private BigDecimal mrp;
    private String status;
    private Long locationId;
    private String locationName;
}