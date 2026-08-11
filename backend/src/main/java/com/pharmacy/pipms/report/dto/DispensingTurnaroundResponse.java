package com.pharmacy.pipms.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DispensingTurnaroundResponse {
    private Double averageTurnaroundMinutes; // null if no data in period
    private long totalDispensingRecordsInPeriod;
}