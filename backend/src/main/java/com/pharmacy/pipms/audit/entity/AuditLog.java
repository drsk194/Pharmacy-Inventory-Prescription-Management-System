package com.pharmacy.pipms.audit.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// Minimal write-only version for now. Module 18 adds search/report
// endpoints, ipAddress/requestId auto-capture via a request filter, and
// full compliance querying. This module only needs the ability to write
// one entry: the FEFO override event.
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_entity", columnList = "entityType,entityId"),
        @Index(name = "idx_audit_action", columnList = "action")
})
@Getter
@Setter
public class AuditLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(length = 100)
    private String entityType;

    private Long entityId;

    @Lob
    private String oldValue;

    @Lob
    private String newValue;

    @Column(length = 20)
    private String result; // "SUCCESS" / "FAILURE"

    @Column(length = 500)
    private String failureReason;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 100)
    private String requestId;
}