package com.pharmacy.pipms.grn.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.purchaseorder.entity.PurchaseOrder;
import com.pharmacy.pipms.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "goods_receipt_notes")
@Getter
@Setter
public class GoodsReceiptNote extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(nullable = false)
    private LocalDateTime receivedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by_id", nullable = false)
    private User receivedBy;

    @Column(nullable = false)
    private int totalItems;

    @Column(length = 1000)
    private String discrepancyNotes;

    @Column(length = 1000)
    private String generalNotes;

    @OneToMany(mappedBy = "goodsReceiptNote", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoodsReceiptItem> items = new ArrayList<>();
}