package com.pharmacy.pipms.notification.dto;

import com.pharmacy.pipms.common.constants.RoleName;
import com.pharmacy.pipms.notification.entity.NotificationPriority;
import com.pharmacy.pipms.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class BroadcastNotificationRequest {

    @NotEmpty(message = "At least one target role is required")
    private Set<RoleName> targetRoles;

    @NotNull(message = "Type is required")
    private NotificationType type;

    @NotNull(message = "Priority is required")
    private NotificationPriority priority;

    @NotBlank(message = "Message is required")
    private String message;
}