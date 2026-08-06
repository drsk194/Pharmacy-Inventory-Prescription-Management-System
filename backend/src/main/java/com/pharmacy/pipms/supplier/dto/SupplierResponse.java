package com.pharmacy.pipms.supplier.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SupplierResponse {
    private Long id;
    private String supplierName;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String drugLicenseNumber;
    private String creditTerms;
    private Double rating;
    private boolean approved;
    private boolean active;
}