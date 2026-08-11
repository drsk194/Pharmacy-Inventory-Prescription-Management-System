package com.pharmacy.pipms.controlledsubstance.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.prescription.entity.Prescription;
import com.pharmacy.pipms.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// The chronological, tamper-evident CS ledger. Matches Appendix B's schema
// exactly (drug_id, transaction_type, quantity, balance_after,
// prescription_id nullable, technician_id, pharmacist_id, witness_id
// nullable, transaction_date, notes) plus two hash-chain columns.
@Entity
@Table(name = "controlled_substance_register", indexes = {
        @Index(name = "idx_csr_drug", columnList = "drug_id"),
        @Index(name = "idx_csr_transaction_date", columnList = "transactionDate")
})
@Getter
@Setter
public class ControlledSubstanceRegister extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_id", nullable = false)
    private Drug drug;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private CsTransactionType transactionType;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal balanceAfter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private User technician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacist_id", nullable = false)
    private User pharmacist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "witness_id")
    private User witness;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Column(length = 500)
    private String notes;

    // Tamper-evident hash chain (SHA-256, not literal blockchain — see
    // module notes). previousHash is "GENESIS" for the very first entry.
    @Column(nullable = false, length = 100)
    private String previousHash;

    @Column(nullable = false, unique = true, length = 100)
    private String entryHash;
}