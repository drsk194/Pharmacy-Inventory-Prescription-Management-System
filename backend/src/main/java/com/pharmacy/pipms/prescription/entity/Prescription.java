package com.pharmacy.pipms.prescription.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.patient.entity.Patient;
import com.pharmacy.pipms.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prescriptions", indexes = {
        @Index(name = "idx_prescription_status", columnList = "status"),
        @Index(name = "idx_prescription_patient", columnList = "patient_id")
})
@Getter
@Setter
public class Prescription extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @Column(nullable = false)
    private LocalDate prescriptionDate;

    @Column(nullable = false)
    private LocalDateTime receiptDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private PrescriptionSource source;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private PrescriptionStatus status;

    // Auto-computed: true if any item's drug is Schedule H, H1, or X.
    @Column(nullable = false)
    private boolean controlled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verifying_pharmacist_id")
    private User verifyingPharmacist;

    @Column(length = 1000)
    private String notes;

    @Column(length = 500)
    private String rejectionReason;

    // Presence-check placeholder for FR5's "authenticity verification" —
    // see Assumption 4 in the module notes. Required (non-blank) only
    // when source = ELECTRONIC.
    @Column(length = 255)
    private String digitalSignatureReference;

    @OneToMany(mappedBy = "prescription", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrescriptionItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "prescription", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<PrescriptionStatusHistory> statusHistory = new ArrayList<>();
}