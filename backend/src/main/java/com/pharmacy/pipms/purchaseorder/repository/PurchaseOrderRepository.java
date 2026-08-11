package com.pharmacy.pipms.purchaseorder.repository;

import com.pharmacy.pipms.purchaseorder.entity.PurchaseOrder;
import com.pharmacy.pipms.purchaseorder.entity.PurchaseOrderStatus;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query("SELECT p FROM PurchaseOrder p WHERE " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:supplierId IS NULL OR p.supplier.id = :supplierId)")
    Page<PurchaseOrder> search(@Param("status") PurchaseOrderStatus status,
                                @Param("supplierId") Long supplierId,
                                Pageable pageable);
    @Query("SELECT p.supplier.id, p.supplier.supplierName, SUM(p.totalValue) FROM PurchaseOrder p " +
           "WHERE p.status IN ('APPROVED','PARTIALLY_RECEIVED','COMPLETED') AND p.orderDate BETWEEN :start AND :end " +
           "GROUP BY p.supplier.id, p.supplier.supplierName ORDER BY SUM(p.totalValue) DESC")
    List<Object[]> sumSpendingBySupplier(@Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);

    @Query("SELECT COALESCE(SUM(p.totalValue), 0) FROM PurchaseOrder p " +
           "WHERE p.status IN ('APPROVED','PARTIALLY_RECEIVED','COMPLETED') AND p.orderDate BETWEEN :start AND :end")
    java.math.BigDecimal sumTotalSpending(@Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);
       
}