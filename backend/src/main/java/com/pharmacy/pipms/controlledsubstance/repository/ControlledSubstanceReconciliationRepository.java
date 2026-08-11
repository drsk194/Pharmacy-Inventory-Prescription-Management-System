package com.pharmacy.pipms.controlledsubstance.repository;

import com.pharmacy.pipms.controlledsubstance.entity.ControlledSubstanceReconciliation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ControlledSubstanceReconciliationRepository extends JpaRepository<ControlledSubstanceReconciliation, Long> {
    List<ControlledSubstanceReconciliation> findByDiscrepancyFlaggedTrueOrderByCreatedAtDesc();
}