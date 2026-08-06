package com.pharmacy.pipms.patient.repository;

import com.pharmacy.pipms.patient.entity.PatientMedication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientMedicationRepository extends JpaRepository<PatientMedication, Long> {
    List<PatientMedication> findByPatientId(Long patientId);
}