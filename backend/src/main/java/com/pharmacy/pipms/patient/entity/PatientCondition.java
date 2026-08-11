package com.pharmacy.pipms.patient.entity;

import com.pharmacy.pipms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

// Gap-fix: Module 4's SRS requirement list included "Medical conditions"
// but it was never built. Added now because Module 10's contraindication
// checking structure needs this data to exist.
@Entity
@Table(name = "patient_conditions")
@Getter
@Setter
public class PatientCondition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false, length = 200)
    private String conditionName; // e.g. "Chronic Kidney Disease", "Hypertension"

    @Column(nullable = false)
    private LocalDate diagnosedDate;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private boolean active = true;
}