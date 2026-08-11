package com.pharmacy.pipms.billing.repository;

import com.pharmacy.pipms.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}