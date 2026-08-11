package com.pharmacy.pipms.batch.repository;

import com.pharmacy.pipms.batch.entity.BatchStatus;
import com.pharmacy.pipms.batch.entity.DrugBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DrugBatchRepository extends JpaRepository<DrugBatch, Long> {

    boolean existsByDrugIdAndBatchNumber(Long drugId, String batchNumber);

    List<DrugBatch> findByDrugIdOrderByExpiryDateAsc(Long drugId);

    Page<DrugBatch> findByStatus(BatchStatus status, Pageable pageable);

    @Query("SELECT b FROM DrugBatch b WHERE " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:locationId IS NULL OR b.location.id = :locationId)")
    Page<DrugBatch> search(@Param("status") BatchStatus status,
                            @Param("locationId") Long locationId,
                            Pageable pageable);

    // FEFO-ready ordering, reused directly by Module 9.
    @Query("SELECT b FROM DrugBatch b WHERE b.drug.id = :drugId AND b.status = 'ACTIVE' " +
           "AND b.currentQuantity > 0 ORDER BY b.expiryDate ASC")
    List<DrugBatch> findEligibleForDispensing(@Param("drugId") Long drugId);

    // Sum of all active-status stock for a drug, for low-stock comparison
    // against Drug.reorderLevel — queried directly rather than via
    // InventoryBalance, per the source-of-truth note on that entity.
    @Query("SELECT COALESCE(SUM(b.currentQuantity), 0) FROM DrugBatch b " +
           "WHERE b.drug.id = :drugId AND b.status IN ('ACTIVE', 'NEAR_EXPIRY')")
    BigDecimal sumActiveQuantityByDrug(@Param("drugId") Long drugId);

    @Query("SELECT b FROM DrugBatch b WHERE b.expiryDate <= :threshold AND b.status IN ('ACTIVE', 'NEAR_EXPIRY')")
    List<DrugBatch> findExpiringBy(@Param("threshold") LocalDate threshold);

    @Query("SELECT b FROM DrugBatch b WHERE b.expiryDate < :today AND b.status NOT IN ('QUARANTINED', 'EXHAUSTED')")
    List<DrugBatch> findNewlyExpired(@Param("today") LocalDate today);

    Page<DrugBatch> findByStatusOrderByExpiryDateAsc(BatchStatus status, Pageable pageable);

    @Query("SELECT b.batchNumber, b.mrp, b.manufacturingDate, b.expiryDate, b.currentQuantity FROM DrugBatch b WHERE b.drug.id = :drugId ORDER BY b.expiryDate ASC")
    List<Object[]> getPriceComparisonForDrug(@Param("drugId") Long drugId);
    @Query("SELECT COALESCE(SUM(b.currentQuantity * b.mrp), 0) FROM DrugBatch b WHERE b.status IN ('ACTIVE','NEAR_EXPIRY')")
    java.math.BigDecimal sumTotalStockValue();

    long countByStatus(BatchStatus status);
    
}