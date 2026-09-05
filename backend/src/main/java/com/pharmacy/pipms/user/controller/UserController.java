package com.pharmacy.pipms.user.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.user.dto.UserProfileResponse;
import com.pharmacy.pipms.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;
import com.pharmacy.pipms.common.constants.RoleName;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and administration")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> myProfile(Authentication authentication) {
        return ApiResponse.success(userService.getProfile(authentication.getName()));
    }
    @GetMapping("/me")
    public ApiResponse<com.pharmacy.pipms.user.dto.UserProfileResponse> me(Authentication authentication) {
        return ApiResponse.success(userService.getProfile(authentication.getName()));
    }

    @GetMapping("/controlled-substance-staff")
    @PreAuthorize("hasAuthority('CONTROLLED_SUBSTANCE_READ') or hasAuthority('CONTROLLED_SUBSTANCE_COSIGN') or hasAuthority('CONTROLLED_SUBSTANCE_AUTHORIZE')")
    public ApiResponse<List<com.pharmacy.pipms.admin.dto.AdminUserSummaryResponse>> controlledSubstanceStaff() {
        return ApiResponse.success(userService.getActiveStaffByRoles(
                Set.of(RoleName.ROLE_TECHNICIAN, RoleName.ROLE_ADMIN, RoleName.ROLE_PHARMACIST)));
    }
}