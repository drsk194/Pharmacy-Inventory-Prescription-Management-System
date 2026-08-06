package com.pharmacy.pipms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Identifier (email, staff ID, or badge number) is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}