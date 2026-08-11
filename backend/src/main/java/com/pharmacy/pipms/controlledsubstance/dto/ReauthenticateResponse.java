package com.pharmacy.pipms.controlledsubstance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReauthenticateResponse {
    private boolean authorized;
    private LocalDateTime expiresAt;
}