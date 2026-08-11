package com.pharmacy.pipms.billing.repository;

import com.pharmacy.pipms.billing.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
}