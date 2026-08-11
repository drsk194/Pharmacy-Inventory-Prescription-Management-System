package com.pharmacy.pipms.controlledsubstance.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "controlled_substance_reconciliations")
@Getter
@Setter
public class ControlledSubstanceReconciliation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_id", nullable = false)
    private Drug drug;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal expectedQuantity;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal countedQuantity;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal variance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_id", nullable = false)
    private User performedBy;

    @Column(nullable = false)
    private boolean discrepancyFlagged;

    // Distinguishes a routine end-of-shift count (false) from an ad hoc
    // /discrepancies report raised outside the formal reconcile flow (true).
    @Column(nullable = false)
    private boolean manuallyReported;

    @Column(length = 500)
    private String notes;
}