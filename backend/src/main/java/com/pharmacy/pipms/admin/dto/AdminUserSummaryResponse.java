package com.pharmacy.pipms.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class AdminUserSummaryResponse {
    private Long id;
    private String fullName;
    private String email;
    private String staffId;
    private Set<String> roles;
    private boolean active;
    private boolean accountLocked;
}