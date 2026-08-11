package com.pharmacy.pipms.schedule.entity;

import com.pharmacy.pipms.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "pharmacy_holidays")
@Getter
@Setter
public class PharmacyHoliday extends BaseEntity {

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Column(nullable = false, length = 150)
    private String description;

    @Column(nullable = false)
    private boolean closed = true; // false = reduced/special hours, see notes
}