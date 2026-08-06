package com.pharmacy.pipms.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class RoleResponse {
    private Long id;
    private String name;
    private String description;
    private Set<String> permissions;
}