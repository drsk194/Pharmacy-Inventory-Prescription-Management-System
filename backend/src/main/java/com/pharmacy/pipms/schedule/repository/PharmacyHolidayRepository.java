package com.pharmacy.pipms.schedule.repository;

import com.pharmacy.pipms.schedule.entity.PharmacyHoliday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PharmacyHolidayRepository extends JpaRepository<PharmacyHoliday, Long> {
    List<PharmacyHoliday> findByDateBetween(LocalDate start, LocalDate end);
    boolean existsByDate(LocalDate date);
}