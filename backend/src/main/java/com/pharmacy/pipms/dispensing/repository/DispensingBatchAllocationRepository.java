package com.pharmacy.pipms.dispensing.repository;

import com.pharmacy.pipms.dispensing.entity.DispensingBatchAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DispensingBatchAllocationRepository extends JpaRepository<DispensingBatchAllocation, Long> {
    List<DispensingBatchAllocation> findByDispensingRecordId(Long dispensingRecordId);
}