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

    // Mandatory and unique — every doctor profile belongs to exactly one
    // staff User account (unlike Patient.user, which is optional).
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 50)
    private String licenseNumber;

    // Not explicitly in the SRS — a reasonable addition for credential context.
    @Column(length = 150)
    private String registrationCouncil;

    @Column(length = 150)
    private String specialization;

    @Column(length = 150)
    private String qualification;

    // FR1's "verification against regulatory database" — mocked as a manual
    // admin action (PATCH .../verify) rather than a live external API call.
    @Column(nullable = false)
    private boolean verified = false;

    // Simplified prescribing-authority model — one flag covering all
    // controlled substances, rather than per-schedule granularity the SRS
    // doesn't specify a schema for.
    @Column(nullable = false)
    private boolean controlledSubstanceAuthorized = false;

    @Column(length = 50)
    private String controlledSubstanceAuthNumber;

    @Column(nullable = false)
    private boolean active = true;
}