package com.pharmacy.pipms.inventory.entity;

import com.pharmacy.pipms.batch.entity.DrugBatch;
import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_movements", indexes = @Index(name = "idx_movement_batch", columnList = "batch_id"))
@Getter
@Setter
public class StockMovement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private DrugBatch batch;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private MovementType movementType;

    // Signed delta: positive = stock increase, negative = stock decrease.
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal balanceAfter;

    @Column(length = 30)
    private String referenceType; // e.g. "ADJUSTMENT", "BATCH_CREATION"

    private Long referenceId;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_id")
    private User performedBy;
}