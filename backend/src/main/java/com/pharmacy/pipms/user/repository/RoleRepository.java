package com.pharmacy.pipms.user.repository;

import com.pharmacy.pipms.common.constants.RoleName;
import com.pharmacy.pipms.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}