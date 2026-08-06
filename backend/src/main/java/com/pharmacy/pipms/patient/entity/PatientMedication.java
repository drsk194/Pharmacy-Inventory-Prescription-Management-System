package com.pharmacy.pipms.patient.entity;

import com.pharmacy.pipms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "patient_medications")
@Getter
@Setter
public class PatientMedication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // Free-text for now — will link to the Drug master (Module 6) once it
    // exists, so externally-prescribed/historical medications not in our
    // catalog can still be recorded.
    @Column(nullable = false, length = 200)
    private String drugName;

    @Column(length = 100)
    private String dosage;

    @Column(length = 100)
    private String frequency;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate; // null = ongoing

    @Column(length = 150)
    private String prescribingDoctor; // free text — may be external to this system

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private boolean active = true; // currently taking vs. historical
}