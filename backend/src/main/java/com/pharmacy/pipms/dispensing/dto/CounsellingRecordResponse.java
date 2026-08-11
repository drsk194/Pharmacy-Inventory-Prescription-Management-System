package com.pharmacy.pipms.dispensing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CounsellingRecordResponse {
    private Long id;
    private Long dispensingRecordId;
    private String pharmacistName;
    private String counsellingType;
    private String notes;
    private LocalDateTime createdAt;
}