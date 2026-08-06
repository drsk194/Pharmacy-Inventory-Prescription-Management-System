package com.pharmacy.pipms.drug.entity;

import com.pharmacy.pipms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "drugs", indexes = {
        @Index(name = "idx_drug_generic_name", columnList = "genericName"),
        @Index(name = "idx_drug_class", columnList = "drugClass")
})
@Getter
@Setter
public class Drug extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String genericName;

    @Column(length = 200)
    private String brandName;

    // Nullable + unique: MySQL allows multiple NULLs in a unique column,
    // so drugs without an NDC code yet don't collide with each other.
    @Column(unique = true, length = 50)
    private String ndcCode;

    @Column(nullable = false, length = 100)
    private String drugClass;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR) // forces VARCHAR — see Module 2's Hibernate 7 enum note
    @Column(nullable = false, length = 20)
    private DrugSchedule schedule;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private StorageCondition storageCondition;

    @Column(nullable = false, length = 20)
    private String unitOfMeasure; // e.g. "tablet", "ml", "vial"

    @Column(nullable = false)
    private Integer reorderLevel;

    @Column(nullable = false)
    private Integer minStockLevel;

    // Nullable — Appendix B doesn't mark this NOT NULL, and not every drug
    // needs a hard ceiling.
    private Integer maxStockLevel;

    @Column(unique = true, length = 100)
    private String barcode;

    @Column(nullable = false)
    private boolean active = true;
}