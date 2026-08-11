package com.pharmacy.pipms.prescription.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.drug.entity.Drug;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "prescription_items")
@Getter
@Setter
public class PrescriptionItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_id", nullable = false)
    private Drug drug;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal prescribedQuantity;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal dispensedQuantity = BigDecimal.ZERO;

    @Column(nullable = false, length = 100)
    private String dosage;

    @Column(nullable = false, length = 100)
    private String frequency;

    @Column(nullable = false, length = 100)
    private String duration;

    @Column(length = 500)
    private String instructions;
}