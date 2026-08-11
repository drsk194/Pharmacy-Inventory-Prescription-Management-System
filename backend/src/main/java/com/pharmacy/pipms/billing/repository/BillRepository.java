package com.pharmacy.pipms.billing.repository;

import com.pharmacy.pipms.billing.entity.Bill;
import com.pharmacy.pipms.billing.entity.BillStatus;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BillRepository extends JpaRepository<Bill, Long> {

    @Query("SELECT b FROM Bill b WHERE " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:patientId IS NULL OR b.patient.id = :patientId)")
    Page<Bill> search(@Param("status") BillStatus status, @Param("patientId") Long patientId, Pageable pageable);

    Page<Bill> findByPatientIdOrderByBillDateDesc(Long patientId, Pageable pageable);

    Page<Bill> findByStatusInOrderByBillDateAsc(java.util.List<BillStatus> statuses, Pageable pageable);
    @Query("SELECT COALESCE(SUM(b.amountPaid), 0) FROM Bill b WHERE b.billDate BETWEEN :start AND :end")
    java.math.BigDecimal sumRevenueInPeriod(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    @Query("SELECT FUNCTION('DATE', b.billDate), SUM(b.amountPaid) FROM Bill b " +
           "WHERE b.billDate BETWEEN :start AND :end GROUP BY FUNCTION('DATE', b.billDate) ORDER BY FUNCTION('DATE', b.billDate)")
    List<Object[]> dailyRevenue(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    @Query("SELECT COALESCE(SUM(b.outstandingAmount), 0) FROM Bill b WHERE b.status IN ('PENDING','PARTIALLY_PAID')")
    java.math.BigDecimal sumOutstanding();

    long countByStatusIn(java.util.List<com.pharmacy.pipms.billing.entity.BillStatus> statuses);
}