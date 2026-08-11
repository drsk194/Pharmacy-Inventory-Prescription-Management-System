package com.pharmacy.pipms.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PrescriptionVolumeResponse {
    private long totalInPeriod;
    private List<LabeledCountResponse> byStatus;
    private List<LabeledCountResponse> bySource;
}