package com.pharmacy.pipms.inventory.repository;

import com.pharmacy.pipms.inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByBatchIdOrderByCreatedAtDesc(Long batchId);
    @Query("SELECT m.batch.drug.id, m.batch.drug.genericName, COUNT(m), SUM(abs(m.quantity)) " +
           "FROM StockMovement m WHERE m.movementType = 'DISPENSING' AND m.createdAt BETWEEN :start AND :end " +
           "GROUP BY m.batch.drug.id, m.batch.drug.genericName ORDER BY SUM(abs(m.quantity)) DESC")
    java.util.List<Object[]> dispensingActivityByDrug(@org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                                                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);
}