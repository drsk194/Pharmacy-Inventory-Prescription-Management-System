package com.pharmacy.pipms.prescription.repository;

import com.pharmacy.pipms.prescription.entity.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {
}