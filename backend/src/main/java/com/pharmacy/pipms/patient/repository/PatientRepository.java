package com.pharmacy.pipms.patient.repository;

import com.pharmacy.pipms.patient.entity.Patient;
import com.pharmacy.pipms.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUser(User user);

    @Query("SELECT p FROM Patient p WHERE " +
           "(:search IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.medicalRecordNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR p.phoneNumber LIKE CONCAT('%', :search, '%'))")
    Page<Patient> search(@Param("search") String search, Pageable pageable);
}