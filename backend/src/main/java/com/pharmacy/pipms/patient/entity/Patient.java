package com.pharmacy.pipms.patient.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patients", indexes = {
        @Index(name = "idx_patient_full_name", columnList = "fullName"),
        @Index(name = "idx_patient_phone", columnList = "phoneNumber")
})
@Getter
@Setter
public class Patient extends BaseEntity {

    // Generated after first save, format "PT-000001" — see PatientService
    @Column(nullable = false, unique = true, length = 20)
    private String medicalRecordNumber;

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR) // forces VARCHAR — see Module 2's Hibernate 7 enum note
    @Column(length = 20)
    private Gender gender;

    @Column(nullable = false, length = 15)
    private String phoneNumber;

    @Column(length = 150)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(length = 150)
    private String emergencyContactName;

    @Column(length = 15)
    private String emergencyContactPhone;

    // Nullable: a patient record can exist before/without a portal login
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false)
    private boolean active = true; // soft delete per SRS Section 14

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PatientAllergy> allergies = new ArrayList<>();

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PatientMedication> medications = new ArrayList<>();
}