package com.pharmacy.pipms.doctor.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "doctor_profiles", indexes = {
        @Index(name = "idx_doctor_license", columnList = "licenseNumber")
})
@Getter
@Setter
public class DoctorProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 50)
    private String licenseNumber;

    @Column(length = 150)
    private String registrationCouncil;

    @Column(length = 150)
    private String specialization;

    @Column(length = 150)
    private String qualification;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(nullable = false)
    private boolean controlledSubstanceAuthorized = false;

    @Column(length = 50)
    private String controlledSubstanceAuthNumber;

    @Column(nullable = false)
    private boolean active = true;
    private java.time.LocalDate licenseExpiryDate;
}