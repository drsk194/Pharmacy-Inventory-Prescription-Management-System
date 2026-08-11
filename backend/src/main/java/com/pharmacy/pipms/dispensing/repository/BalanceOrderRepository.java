package com.pharmacy.pipms.dispensing.repository;

import com.pharmacy.pipms.dispensing.entity.BalanceOrder;
import com.pharmacy.pipms.dispensing.entity.BalanceOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceOrderRepository extends JpaRepository<BalanceOrder, Long> {
    List<BalanceOrder> findByPrescriptionItemIdAndStatus(Long prescriptionItemId, BalanceOrderStatus status);
    List<BalanceOrder> findByStatus(BalanceOrderStatus status);
}