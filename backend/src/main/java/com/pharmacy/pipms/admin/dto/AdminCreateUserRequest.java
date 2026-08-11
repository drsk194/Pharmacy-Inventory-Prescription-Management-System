package com.pharmacy.pipms.admin.dto;

import com.pharmacy.pipms.common.constants.RoleName;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCreateUserRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank @Email(message = "Invalid email format")
    private String email;

    private String staffId;

    @NotBlank(message = "Temporary password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$",
            message = "Password must be at least 8 characters and include upper, lower, digit, and special character")
    private String temporaryPassword;

    @NotNull(message = "Role is required")
    private RoleName role;

    private String phoneNumber;
    private String licenseNumber;
}