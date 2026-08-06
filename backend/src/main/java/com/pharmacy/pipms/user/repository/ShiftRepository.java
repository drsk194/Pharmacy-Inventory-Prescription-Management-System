package com.pharmacy.pipms.user.repository;

import com.pharmacy.pipms.user.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
}