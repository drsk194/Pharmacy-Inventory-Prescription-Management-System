package com.pharmacy.pipms.grn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class GoodsReceiptItemResponse {
    private Long id;
    private Long purchaseOrderItemId;
    private String drugGenericName;
    private String batchNumber;
    private BigDecimal expectedQuantity;
    private BigDecimal receivedQuantity;
    private BigDecimal quantityDiscrepancy;
    private boolean qualityDiscrepancy;
    private String qualityNotes;
    private String conditionNotes;
    private Long createdBatchId;
}