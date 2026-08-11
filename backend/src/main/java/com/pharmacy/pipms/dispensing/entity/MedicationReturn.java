package com.pharmacy.pipms.dispensing.entity;

import com.pharmacy.pipms.batch.entity.DrugBatch;
import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "medication_returns")
@Getter
@Setter
public class MedicationReturn extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispensing_record_id", nullable = false)
    private DispensingRecord dispensingRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private DrugBatch batch;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityReturned;

    @Column(length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_id", nullable = false)
    private User processedBy;
}