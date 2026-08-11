package com.pharmacy.pipms.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String message;
    private String type;
    private String priority;
    private boolean isRead;
    private LocalDateTime readAt;
    private boolean escalated;
    private String referenceType;
    private Long referenceId;
    private LocalDateTime createdAt;
}