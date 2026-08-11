package com.pharmacy.pipms.billing.repository;

import com.pharmacy.pipms.billing.entity.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillItemRepository extends JpaRepository<BillItem, Long> {
    boolean existsByDispensingRecordId(Long dispensingRecordId);
    Optional<BillItem> findByDispensingRecordId(Long dispensingRecordId);
}