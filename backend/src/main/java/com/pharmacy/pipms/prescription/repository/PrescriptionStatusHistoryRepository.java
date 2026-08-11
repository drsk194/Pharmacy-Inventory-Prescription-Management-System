package com.pharmacy.pipms.prescription.repository;

import com.pharmacy.pipms.prescription.entity.PrescriptionStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionStatusHistoryRepository extends JpaRepository<PrescriptionStatusHistory, Long> {
    List<PrescriptionStatusHistory> findByPrescriptionIdOrderByCreatedAtAsc(Long prescriptionId);
}