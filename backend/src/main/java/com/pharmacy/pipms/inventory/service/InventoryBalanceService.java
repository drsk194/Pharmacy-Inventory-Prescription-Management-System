package com.pharmacy.pipms.inventory.service;

import com.pharmacy.pipms.drug.entity.Drug;
import com.pharmacy.pipms.inventory.entity.InventoryBalance;
import com.pharmacy.pipms.inventory.entity.InventoryLocation;
import com.pharmacy.pipms.inventory.repository.InventoryBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class InventoryBalanceService {

    private final InventoryBalanceRepository balanceRepository;

    @Transactional
    public void adjust(Drug drug, InventoryLocation location, BigDecimal delta) {
        InventoryBalance balance = balanceRepository.findByDrugIdAndLocationId(drug.getId(), location.getId())
                .orElseGet(() -> {
                    InventoryBalance b = new InventoryBalance();
                    b.setDrug(drug);
                    b.setLocation(location);
                    b.setTotalQuantity(BigDecimal.ZERO);
                    return b;
                });
        balance.setTotalQuantity(balance.getTotalQuantity().add(delta));
        balanceRepository.save(balance);
    }
}