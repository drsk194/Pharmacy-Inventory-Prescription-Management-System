package com.pharmacy.pipms.user.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.user.dto.UserProfileResponse;
import com.pharmacy.pipms.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}