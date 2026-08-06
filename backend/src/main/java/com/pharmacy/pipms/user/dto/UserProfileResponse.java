package com.pharmacy.pipms.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String fullName;
    private String email;
    private String staffId;
    private Set<String> roles;
    private String phoneNumber;
    private boolean active;
    private String licenseNumber;
    private boolean controlledSubstanceAuthorized;
    private LocalDateTime lastLogin;
}