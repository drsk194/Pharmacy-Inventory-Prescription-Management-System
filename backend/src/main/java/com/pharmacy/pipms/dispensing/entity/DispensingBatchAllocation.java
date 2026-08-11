package com.pharmacy.pipms.dispensing.entity;

import com.pharmacy.pipms.batch.entity.DrugBatch;
import com.pharmacy.pipms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// Records exactly which batch(es) a DispensingRecord's quantity came from —
// FEFO can split one dispensing event across multiple batches.
@Entity
@Table(name = "dispensing_batch_allocations")
@Getter
@Setter
public class DispensingBatchAllocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispensing_record_id", nullable = false)
    private DispensingRecord dispensingRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private DrugBatch batch;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityAllocated;
}