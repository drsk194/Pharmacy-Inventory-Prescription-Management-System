package com.pharmacy.pipms.auth.entity;

import com.pharmacy.pipms.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_blacklist")
@Getter
@Setter
public class TokenBlacklist extends BaseEntity {

    // We store the JWT ID (jti), not the whole token, to keep this table small
    @Column(nullable = false, unique = true, length = 100)
    private String jti;

    @Column(nullable = false)
    private LocalDateTime expiresAt; // matches original token expiry; row can be purged after this
}