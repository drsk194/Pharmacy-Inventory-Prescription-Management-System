package com.pharmacy.pipms.dispensing.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.prescription.entity.PrescriptionItem;
import com.pharmacy.pipms.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dispensing_records")
@Getter
@Setter
public class DispensingRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_item_id", nullable = false)
    private PrescriptionItem prescriptionItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private User technician;

    // Nullable until authorize() sets it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacist_id")
    private User pharmacist;

    // Set at prepare-time: min(remaining prescribed quantity, available stock)
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityIntended;

    // Set at authorize-time: what was actually consumed via fresh FEFO
    @Column(precision = 12, scale = 3)
    private BigDecimal quantityDispensed;

    @Column(length = 100)
    private String scannedBarcode;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private DispensingStatus status;

    @Column(nullable = false)
    private boolean labelPrinted = false;

    private LocalDateTime dispensedAt;

    @Column(nullable = false)
    private boolean patientAcknowledged = false;

    @Column(length = 150)
    private String acknowledgedByName;

    @Column(length = 20)
    private String acknowledgedRelation; // "SELF" or "CAREGIVER"

    @OneToMany(mappedBy = "dispensingRecord", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DispensingBatchAllocation> batchAllocations = new ArrayList<>();
}