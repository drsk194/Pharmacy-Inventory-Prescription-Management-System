package com.pharmacy.pipms.systemconfig.service;

import com.pharmacy.pipms.exception.DuplicateResourceException;
import com.pharmacy.pipms.exception.ResourceNotFoundException;
import com.pharmacy.pipms.systemconfig.dto.SystemConfigurationRequest;
import com.pharmacy.pipms.systemconfig.dto.SystemConfigurationResponse;
import com.pharmacy.pipms.systemconfig.entity.SystemConfiguration;
import com.pharmacy.pipms.systemconfig.repository.SystemConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemConfigurationService {

    private final SystemConfigurationRepository repository;

    @Transactional
    public SystemConfigurationResponse create(SystemConfigurationRequest request) {
        if (repository.findByConfigKey(request.getConfigKey()).isPresent()) {
            throw new DuplicateResourceException("Configuration key already exists: " + request.getConfigKey());
        }
        SystemConfiguration config = new SystemConfiguration();
        applyFields(config, request);
        return toResponse(repository.save(config));
    }

    @Transactional
    public SystemConfigurationResponse update(Long id, SystemConfigurationRequest request) {
        SystemConfiguration config = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration not found: " + id));
        applyFields(config, request);
        return toResponse(repository.save(config));
    }

    @Transactional(readOnly = true)
    public List<SystemConfigurationResponse> getAll(String category) {
        List<SystemConfiguration> list = category != null ? repository.findByCategory(category) : repository.findAll();
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SystemConfigurationResponse getByKey(String key) {
        return toResponse(repository.findByConfigKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration key not found: " + key)));
    }

    private void applyFields(SystemConfiguration config, SystemConfigurationRequest request) {
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setDataType(request.getDataType());
        config.setCategory(request.getCategory());
        config.setDescription(request.getDescription());
    }

    private SystemConfigurationResponse toResponse(SystemConfiguration c) {
        return new SystemConfigurationResponse(c.getId(), c.getConfigKey(), c.getConfigValue(),
                c.getDataType().name(), c.getCategory(), c.getDescription());
    }
}