package com.pharmacy.pipms.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminAnalyticsResponse {
    private long prescriptionQueueCount;
    private long lowStockAlertCount;
    private long dispensingVolumeToday;
}