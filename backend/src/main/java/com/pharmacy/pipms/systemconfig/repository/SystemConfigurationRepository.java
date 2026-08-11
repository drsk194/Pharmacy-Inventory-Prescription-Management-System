package com.pharmacy.pipms.systemconfig.repository;

import com.pharmacy.pipms.systemconfig.entity.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, Long> {
    Optional<SystemConfiguration> findByConfigKey(String configKey);
    List<SystemConfiguration> findByCategory(String category);
}