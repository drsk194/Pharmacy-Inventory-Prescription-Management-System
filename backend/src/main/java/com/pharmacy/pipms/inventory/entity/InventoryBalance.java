package com.pharmacy.pipms.inventory.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.drug.entity.Drug;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// A maintained (write-time-updated) aggregate of total quantity per
// drug+location, kept in sync transactionally whenever a batch's quantity
// changes. This is a read-optimization cache for multi-location dashboards
// — low-stock detection itself queries drug_batches directly (the real
// source of truth) rather than trusting this cache alone, to avoid any
// risk of drift silently causing incorrect reorder alerts.
@Entity
@Table(name = "inventory_balances",
        uniqueConstraints = @UniqueConstraint(name = "uk_balance_drug_location", columnNames = {"drug_id", "location_id"}))
@Getter
@Setter
public class InventoryBalance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_id", nullable = false)
    private Drug drug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private InventoryLocation location;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal totalQuantity = BigDecimal.ZERO;
}