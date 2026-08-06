package com.pharmacy.pipms.inventory.repository;

import com.pharmacy.pipms.inventory.entity.InventoryBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, Long> {
    Optional<InventoryBalance> findByDrugIdAndLocationId(Long drugId, Long locationId);
}