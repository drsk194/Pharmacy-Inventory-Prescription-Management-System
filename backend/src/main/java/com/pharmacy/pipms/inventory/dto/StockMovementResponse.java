package com.pharmacy.pipms.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StockMovementResponse {
    private Long id;
    private String movementType;
    private BigDecimal quantity;
    private BigDecimal balanceAfter;
    private String referenceType;
    private Long referenceId;
    private String notes;
    private String performedByName;
    private LocalDateTime createdAt;
}