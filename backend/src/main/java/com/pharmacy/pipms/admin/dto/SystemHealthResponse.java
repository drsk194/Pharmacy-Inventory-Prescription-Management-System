package com.pharmacy.pipms.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SystemHealthResponse {
    private boolean databaseConnected;
    private long totalUsers;
    private long activeUsers;
    private long totalDrugs;
    private long pendingPrescriptionQueueCount;
    private long lowStockDrugCount;
    private LocalDateTime checkedAt;
}