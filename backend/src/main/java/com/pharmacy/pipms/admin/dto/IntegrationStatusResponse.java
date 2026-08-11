package com.pharmacy.pipms.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IntegrationStatusResponse {
    private String integrationName;
    private String status; // CONFIGURED / NOT_CONFIGURED / MOCK
    private String note;
}