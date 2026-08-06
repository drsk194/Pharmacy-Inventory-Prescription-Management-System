package com.pharmacy.pipms.inventory.repository;

import com.pharmacy.pipms.inventory.entity.InventoryLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryLocationRepository extends JpaRepository<InventoryLocation, Long> {
    boolean existsByName(String name);
}