package com.pharmacy.pipms.supplier.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierUpdateRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 200)
    private String supplierName;

    @Size(max = 150)
    private String contactPerson;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 255)
    private String address;

    @NotBlank(message = "Drug license number is required")
    @Size(max = 50)
    private String drugLicenseNumber;

    @Size(max = 100)
    private String creditTerms;

    @DecimalMin(value = "0.0", message = "Rating must be between 0.0 and 5.0")
    @DecimalMax(value = "5.0", message = "Rating must be between 0.0 and 5.0")
    private Double rating;
}