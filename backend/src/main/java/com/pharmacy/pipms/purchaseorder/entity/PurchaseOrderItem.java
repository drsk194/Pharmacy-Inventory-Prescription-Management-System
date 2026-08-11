package com.pharmacy.pipms.purchaseorder.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.drug.entity.Drug;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "po_items")
@Getter
@Setter
public class PurchaseOrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_id", nullable = false)
    private Drug drug;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal orderedQuantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    // Updated by Module 14 (GRN) as stock actually arrives; starts at zero.
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal receivedQuantity = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private PurchaseOrderItemStatus status = PurchaseOrderItemStatus.PENDING;
}