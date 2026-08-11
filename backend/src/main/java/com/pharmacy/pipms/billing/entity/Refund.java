package com.pharmacy.pipms.billing.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.dispensing.entity.MedicationReturn;
import com.pharmacy.pipms.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Getter
@Setter
public class Refund extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    // Nullable — a refund may stem from a medication return (Module 11) or
    // simply be a billing correction with no associated inventory event.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_return_id")
    private MedicationReturn medicationReturn;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_id", nullable = false)
    private User processedBy;

    @Column(nullable = false)
    private LocalDateTime refundDate = LocalDateTime.now();
}