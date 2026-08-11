package com.pharmacy.pipms.grn.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.purchaseorder.entity.PurchaseOrderItem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "goods_receipt_items")
@Getter
@Setter
public class GoodsReceiptItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_receipt_note_id", nullable = false)
    private GoodsReceiptNote goodsReceiptNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_item_id", nullable = false)
    private PurchaseOrderItem purchaseOrderItem;

    @Column(nullable = false, length = 50)
    private String batchNumber;

    @Column(nullable = false)
    private LocalDate manufacturingDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    // What remained on the PO item at the moment this GRN was processed —
    // stored for audit even though PurchaseOrderItem itself keeps updating.
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal expectedQuantity;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal receivedQuantity;

    // receivedQuantity - expectedQuantity; positive = over-delivery, negative = short.
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityDiscrepancy;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal mrp;

    @Column(length = 500)
    private String conditionNotes;

    @Column(nullable = false)
    private boolean qualityDiscrepancy = false;

    @Column(length = 500)
    private String qualityNotes;

    // Not a real FK — same reasoning as DrugBatch.grnId (see module notes).
    private Long createdBatchId;
}