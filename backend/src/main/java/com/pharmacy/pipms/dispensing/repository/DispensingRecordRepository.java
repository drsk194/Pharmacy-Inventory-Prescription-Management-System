package com.pharmacy.pipms.dispensing.repository;

import com.pharmacy.pipms.dispensing.entity.DispensingRecord;
import com.pharmacy.pipms.dispensing.entity.DispensingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DispensingRecordRepository extends JpaRepository<DispensingRecord, Long> {

    @Query("SELECT d FROM DispensingRecord d WHERE (:status IS NULL OR d.status = :status)")
    Page<DispensingRecord> search(@Param("status") DispensingStatus status, Pageable pageable);
    // MySQL-specific TIMESTAMPDIFF — the one deliberate exception to
    // database-agnostic JPA in this project. See Module 17's Assumption 5.
    @Query(value = "SELECT AVG(TIMESTAMPDIFF(MINUTE, p.receipt_date, d.dispensed_at)) " +
           "FROM dispensing_records d " +
           "JOIN prescription_items pi ON d.prescription_item_id = pi.id " +
           "JOIN prescriptions p ON pi.prescription_id = p.id " +
           "WHERE d.dispensed_at IS NOT NULL AND d.dispensed_at BETWEEN :start AND :end",
           nativeQuery = true)
    Double averageTurnaroundMinutes(@Param("start") java.time.LocalDateTime start,
                                    @Param("end") java.time.LocalDateTime end);

    @Query("SELECT d.technician.id, d.technician.fullName, COUNT(d) FROM DispensingRecord d " +
           "WHERE d.createdAt BETWEEN :start AND :end GROUP BY d.technician.id, d.technician.fullName")
    java.util.List<Object[]> countByTechnician(@org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                                                @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

    @Query("SELECT d.pharmacist.id, d.pharmacist.fullName, COUNT(d) FROM DispensingRecord d " +
           "WHERE d.pharmacist IS NOT NULL AND d.dispensedAt BETWEEN :start AND :end " +
           "GROUP BY d.pharmacist.id, d.pharmacist.fullName")
    java.util.List<Object[]> countByPharmacist(@org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                                                @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

    @Query("SELECT COUNT(d) FROM DispensingRecord d WHERE d.createdAt BETWEEN :start AND :end")
    long countInPeriod(@org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);
}