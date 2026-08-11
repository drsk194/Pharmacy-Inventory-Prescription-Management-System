package com.pharmacy.pipms.notification.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user_read", columnList = "user_id,isRead")
})
@Getter
@Setter
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private NotificationPriority priority;

    @Column(nullable = false)
    private boolean isRead = false;

    private LocalDateTime readAt;

    @Column(nullable = false)
    private boolean escalated = false;

    private LocalDateTime escalatedAt;

    // Not a real FK — same "reference, not relation" pattern used
    // throughout this project (e.g. DrugBatch.grnId).
    @Column(length = 30)
    private String referenceType;

    private Long referenceId;
}