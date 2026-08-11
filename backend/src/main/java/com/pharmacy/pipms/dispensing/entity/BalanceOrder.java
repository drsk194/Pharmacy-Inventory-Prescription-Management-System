package com.pharmacy.pipms.dispensing.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.prescription.entity.PrescriptionItem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "balance_orders")
@Getter
@Setter
public class BalanceOrder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_item_id", nullable = false)
    private PrescriptionItem prescriptionItem;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityPending;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private BalanceOrderStatus status;
}