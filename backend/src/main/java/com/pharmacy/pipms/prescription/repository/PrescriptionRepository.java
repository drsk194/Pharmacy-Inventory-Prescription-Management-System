package com.pharmacy.pipms.prescription.repository;

import com.pharmacy.pipms.prescription.entity.Prescription;
import com.pharmacy.pipms.prescription.entity.PrescriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    @Query("SELECT p FROM Prescription p WHERE " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:patientId IS NULL OR p.patient.id = :patientId) AND " +
           "(:doctorId IS NULL OR p.doctor.id = :doctorId) AND " +
           "(:controlledOnly = false OR p.controlled = true)")
    Page<Prescription> search(@Param("status") PrescriptionStatus status,
                               @Param("patientId") Long patientId,
                               @Param("doctorId") Long doctorId,
                               @Param("controlledOnly") boolean controlledOnly,
                               Pageable pageable);

    List<Prescription> findByStatusInOrderByReceiptDateAsc(List<PrescriptionStatus> statuses);

    Page<Prescription> findByPatientIdOrderByReceiptDateDesc(Long patientId, Pageable pageable);

    Page<Prescription> findByDoctorIdOrderByReceiptDateDesc(Long doctorId, Pageable pageable);
    @Query("SELECT p.status, COUNT(p) FROM Prescription p WHERE p.receiptDate BETWEEN :start AND :end GROUP BY p.status")
    List<Object[]> countByStatusGrouped(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    @Query("SELECT p.source, COUNT(p) FROM Prescription p WHERE p.receiptDate BETWEEN :start AND :end GROUP BY p.source")
    List<Object[]> countBySourceGrouped(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.receiptDate BETWEEN :start AND :end")
    long countInPeriod(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}