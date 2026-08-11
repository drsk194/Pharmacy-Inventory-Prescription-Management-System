package com.pharmacy.pipms.auth.controller;

import com.pharmacy.pipms.auth.dto.*;
import com.pharmacy.pipms.auth.service.AuthService;
import com.pharmacy.pipms.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, login, tokens, password management")

public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthService.UserProfileLikeResult> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("Registration successful", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successful", authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success("Token refreshed", authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authHeader,
                                     @Valid @RequestBody RefreshTokenRequest request) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token, request);
        return ApiResponse.success("Logged out successfully", null);
    }

    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAll(Authentication authentication) {
        authService.logoutAll(authentication.getName());
        return ApiResponse.success("Logged out from all devices", null);
    }

    @GetMapping("/me")
    public ApiResponse<String> me(Authentication authentication) {
        return ApiResponse.success(authentication.getName());
    }

    @PutMapping("/change-password")
    public ApiResponse<Void> changePassword(Authentication authentication,
                                             @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(authentication.getName(), request);
        return ApiResponse.success("Password changed successfully", null);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.success("If the email exists, an OTP has been sent", null);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("Password reset successfully", null);
    }
    @PutMapping("/controlled-substance-pin")
    public ApiResponse<Void> setControlledSubstancePin(Authentication authentication,
                                                         @Valid @RequestBody SetControlledSubstancePinRequest request) {
        authService.setControlledSubstancePin(authentication.getName(), request);
        return ApiResponse.success("Controlled-substance PIN set successfully", null);
    }
}