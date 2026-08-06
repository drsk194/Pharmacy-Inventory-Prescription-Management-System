package com.pharmacy.pipms.batch.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.inventory.entity.InventoryLocation;
import com.pharmacy.pipms.supplier.entity.Supplier;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "drug_batches",
        indexes = {
                @Index(name = "idx_batch_expiry", columnList = "expiryDate"),
                @Index(name = "idx_batch_status", columnList = "status")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_drug_batch_number", columnNames = {"drug_id", "batch_number"}))
@Getter
@Setter
public class DrugBatch extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_id", nullable = false)
    private Drug drug;

    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;

    @Column(nullable = false)
    private LocalDate manufacturingDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityReceived;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal currentQuantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal mrp;

    // Not a real FK yet — Module 14 (GRN) doesn't exist. Populated once it does.
    private Long grnId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private BatchStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private InventoryLocation location;
}