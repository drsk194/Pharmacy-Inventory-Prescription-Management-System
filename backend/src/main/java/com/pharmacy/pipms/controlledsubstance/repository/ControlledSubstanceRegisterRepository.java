package com.pharmacy.pipms.controlledsubstance.repository;

import com.pharmacy.pipms.controlledsubstance.entity.ControlledSubstanceRegister;
import com.pharmacy.pipms.controlledsubstance.entity.CsTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ControlledSubstanceRegisterRepository extends JpaRepository<ControlledSubstanceRegister, Long> {

    Optional<ControlledSubstanceRegister> findTopByOrderByIdDesc();

    List<ControlledSubstanceRegister> findAllByOrderByIdAsc();

    @Query("SELECT r FROM ControlledSubstanceRegister r WHERE " +
           "(:drugId IS NULL OR r.drug.id = :drugId) AND " +
           "(:type IS NULL OR r.transactionType = :type) " +
           "ORDER BY r.transactionDate DESC")
    Page<ControlledSubstanceRegister> search(@Param("drugId") Long drugId,
                                              @Param("type") CsTransactionType type,
                                              Pageable pageable);

    long countByPrescriptionIdAndTransactionType(Long prescriptionId, CsTransactionType type);

    List<ControlledSubstanceRegister> findByDrugIdOrderByTransactionDateAsc(Long drugId);
}