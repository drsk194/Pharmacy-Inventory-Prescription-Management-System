package com.pharmacy.pipms.user.entity;

import com.pharmacy.pipms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Nullable: patients typically won't have a staff ID
    @Column(unique = true, length = 20)
    private String staffId;

    // Nullable: only used for staff badge login
    @Column(unique = true, length = 30)
    private String badgeNumber;

    @Column(nullable = false)
    private String passwordHash;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(length = 15)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean accountLocked = false;

    private LocalDateTime lockedUntil;

    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    private LocalDateTime lastLogin;

    private LocalDateTime passwordChangedAt;

    // FR1: 60-day mandatory expiry for pharmacy staff (not enforced for patients)
    private LocalDateTime passwordExpiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift;

    // Pharmacist/doctor licence info (Section: FR1, FR5)
    @Column(length = 50)
    private String licenseNumber;

    // Module 12 will use this heavily; flag lives on the user from day one
    @Column(nullable = false)
    private boolean controlledSubstanceAuthorized = false;
}