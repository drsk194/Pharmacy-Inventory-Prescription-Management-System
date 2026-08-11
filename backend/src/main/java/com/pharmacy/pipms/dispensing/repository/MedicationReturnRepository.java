package com.pharmacy.pipms.dispensing.repository;

import com.pharmacy.pipms.dispensing.entity.MedicationReturn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationReturnRepository extends JpaRepository<MedicationReturn, Long> {
}