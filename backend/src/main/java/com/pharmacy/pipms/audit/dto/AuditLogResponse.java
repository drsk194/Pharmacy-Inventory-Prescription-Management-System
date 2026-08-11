package com.pharmacy.pipms.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private String userEmail;
    private String action;
    private String entityType;
    private Long entityId;
    private String oldValue;
    private String newValue;
    private String result;
    private String failureReason;
    private String ipAddress;
    private String requestId;
    private LocalDateTime timestamp;
}