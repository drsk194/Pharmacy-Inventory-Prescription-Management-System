package com.pharmacy.pipms.user.repository;

import com.pharmacy.pipms.common.constants.PermissionName;
import com.pharmacy.pipms.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(PermissionName name);
}