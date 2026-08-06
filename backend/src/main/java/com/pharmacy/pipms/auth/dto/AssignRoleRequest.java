package com.pharmacy.pipms.auth.dto;

import com.pharmacy.pipms.common.constants.RoleName;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class AssignRoleRequest {

    @NotEmpty(message = "At least one role is required")
    private Set<RoleName> roleNames;
}