package com.pharmacy.pipms.dispensing.repository;

import com.pharmacy.pipms.dispensing.entity.DispensingError;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispensingErrorRepository extends JpaRepository<DispensingError, Long> {
}