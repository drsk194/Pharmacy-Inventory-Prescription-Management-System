package com.pharmacy.pipms.supplier.entity;

import com.pharmacy.pipms.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "suppliers", indexes = {
        @Index(name = "idx_supplier_name", columnList = "supplierName"),
        @Index(name = "idx_supplier_license", columnList = "drugLicenseNumber")
})
@Getter
@Setter
public class Supplier extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String supplierName;

    @Column(length = 150)
    private String contactPerson;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(unique = true, length = 150)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(nullable = false, unique = true, length = 50)
    private String drugLicenseNumber;

    @Column(length = 100)
    private String creditTerms; // free text, e.g. "Net 30", "COD"

    private Double rating;

    @Column(nullable = false)
    private boolean approved = false;

    @Column(nullable = false)
    private boolean active = true;
}