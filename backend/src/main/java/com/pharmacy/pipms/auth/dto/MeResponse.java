package com.pharmacy.pipms.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class MeResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String staffId;
    private Set<String> roles;
}
