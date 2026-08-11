package com.pharmacy.pipms.controlledsubstance.repository;

import com.pharmacy.pipms.controlledsubstance.entity.ControlledSubstanceAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ControlledSubstanceAuthorizationRepository extends JpaRepository<ControlledSubstanceAuthorization, Long> {
    Optional<ControlledSubstanceAuthorization> findTopByUserIdOrderByExpiresAtDesc(Long userId);
}