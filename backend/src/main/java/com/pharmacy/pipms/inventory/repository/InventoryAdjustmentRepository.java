package com.pharmacy.pipms.inventory.repository;

import com.pharmacy.pipms.inventory.entity.AdjustmentReasonCode;
import com.pharmacy.pipms.inventory.entity.AdjustmentStatus;
import com.pharmacy.pipms.inventory.entity.InventoryAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryAdjustmentRepository extends JpaRepository<InventoryAdjustment, Long> {

    @Query("SELECT a FROM InventoryAdjustment a WHERE " +
           "(:status IS NULL OR a.status = :status)")
    Page<InventoryAdjustment> search(@Param("status") AdjustmentStatus status, Pageable pageable);

    @Query("SELECT a FROM InventoryAdjustment a WHERE a.reasonCode = :reason " +
           "AND a.createdAt BETWEEN :start AND :end ORDER BY a.createdAt DESC")
    List<InventoryAdjustment> findVarianceReport(@Param("reason") AdjustmentReasonCode reason,
                                                   @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);
}