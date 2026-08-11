package com.pharmacy.pipms.dispensing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DispensingErrorResponse {
    private Long id;
    private Long dispensingRecordId;
    private String errorType;
    private String description;
    private String correctiveAction;
    private String reportedByName;
    private LocalDateTime createdAt;
}