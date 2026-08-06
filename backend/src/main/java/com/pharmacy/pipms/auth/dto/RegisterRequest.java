package com.pharmacy.pipms.auth.dto;

import com.pharmacy.pipms.common.constants.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 150, message = "Full name must be 2-150 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 4, max = 20, message = "Staff ID must be 4-20 alphanumeric characters")
    @Pattern(regexp = "^[A-Za-z0-9]*$", message = "Staff ID must be alphanumeric")
    private String staffId; // required for staff roles; null for patients

    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$",
        message = "Password must be at least 8 characters and include upper, lower, digit, and special character"
    )
    private String password;

    @NotNull(message = "Role is required")
    private RoleName role;

    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    private String licenseNumber; // required for ROLE_PHARMACIST / ROLE_DOCTOR — checked in service layer
}