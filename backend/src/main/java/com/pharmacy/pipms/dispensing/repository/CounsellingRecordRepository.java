package com.pharmacy.pipms.dispensing.repository;

import com.pharmacy.pipms.dispensing.entity.CounsellingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CounsellingRecordRepository extends JpaRepository<CounsellingRecord, Long> {
    List<CounsellingRecord> findByDispensingRecordId(Long dispensingRecordId);
}