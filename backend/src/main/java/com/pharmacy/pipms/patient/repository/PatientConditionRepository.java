package com.pharmacy.pipms.patient.repository;

import com.pharmacy.pipms.patient.entity.PatientCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientConditionRepository extends JpaRepository<PatientCondition, Long> {
    List<PatientCondition> findByPatientIdAndActiveTrue(Long patientId);
}