package com.pharmacy.pipms.user.controller;

import com.pharmacy.pipms.auth.dto.AssignRoleRequest;
import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.user.dto.UserProfileResponse;
import com.pharmacy.pipms.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - User Management", description = "Admin-only account status and role assignment")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @PatchMapping("/{id}/status")
    public ApiResponse<UserProfileResponse> setStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponse.success(
                active ? "Account activated" : "Account deactivated",
                userService.setActive(id, active));
    }

    @PatchMapping("/{id}/roles")
    public ApiResponse<UserProfileResponse> assignRoles(@PathVariable Long id,@Valid @RequestBody AssignRoleRequest request) {
        return ApiResponse.success("Roles updated", userService.assignRoles(id, request.getRoleNames()));
    }
}