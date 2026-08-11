package com.pharmacy.pipms.controlledsubstance.entity;

import com.pharmacy.pipms.common.BaseEntity;
import com.pharmacy.pipms.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// One row per successful PIN re-authentication — represents an active
// 30-minute "unlocked" session for controlled-substance work.
@Entity
@Table(name = "controlled_substance_authorizations")
@Getter
@Setter
public class ControlledSubstanceAuthorization extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime authorizedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}