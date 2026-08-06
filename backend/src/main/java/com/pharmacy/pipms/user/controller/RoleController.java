package com.pharmacy.pipms.user.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.user.dto.PermissionResponse;
import com.pharmacy.pipms.user.dto.RoleResponse;
import com.pharmacy.pipms.user.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Roles & Permissions", description = "Admin-only lookup of roles and their granular permissions")
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<RoleResponse>> getAllRoles() {
        return ApiResponse.success(roleService.getAllRoles());
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<PermissionResponse>> getAllPermissions() {
        return ApiResponse.success(roleService.getAllPermissions());
    }
}