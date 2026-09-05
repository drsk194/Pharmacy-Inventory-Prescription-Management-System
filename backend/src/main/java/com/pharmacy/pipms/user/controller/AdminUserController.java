package com.pharmacy.pipms.user.controller;

import com.pharmacy.pipms.auth.dto.AssignRoleRequest;
import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.user.dto.UserProfileResponse;
import com.pharmacy.pipms.user.service.UserService;
import com.pharmacy.pipms.auth.service.AuthService;
import com.pharmacy.pipms.admin.dto.AdminSetControlledSubstancePinRequest;
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
    private final AuthService authService;

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

    @PutMapping("/{id}/controlled-substance-pin")
    public ApiResponse<UserProfileResponse> setControlledSubstancePin(
            @PathVariable Long id, @Valid @RequestBody AdminSetControlledSubstancePinRequest request) {
        return ApiResponse.success("Controlled-substance PIN provisioned",
                userService.setControlledSubstancePin(id, request.getNewPin()));
    }
    @PostMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<?> createUser(@jakarta.validation.Valid @RequestBody com.pharmacy.pipms.admin.dto.AdminCreateUserRequest request) {
        return ApiResponse.success("Staff account created", authService.adminCreateUser(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<com.pharmacy.pipms.common.PageResponse<com.pharmacy.pipms.admin.dto.AdminUserSummaryResponse>> search(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(userService.searchUsers(search, org.springframework.data.domain.PageRequest.of(page, size)));
    }
}