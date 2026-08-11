package com.pharmacy.pipms.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ComplianceDashboardResponse {
    private long doctorsWithLicenseExpiringWithin30Days;
    private long unverifiedDoctorCount;
    private long unresolvedControlledSubstanceDiscrepancies;
    private String note;
}