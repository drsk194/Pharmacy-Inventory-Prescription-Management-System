package com.pharmacy.pipms.systemconfig.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.systemconfig.dto.SystemConfigurationRequest;
import com.pharmacy.pipms.systemconfig.dto.SystemConfigurationResponse;
import com.pharmacy.pipms.systemconfig.service.SystemConfigurationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@Tag(name = "System Configuration", description = "Generic operational settings not governed elsewhere — see Module 19 notes")
@PreAuthorize("hasAuthority('SYSTEM_CONFIGURE')")
public class SystemConfigurationController {

    private final SystemConfigurationService service;

    @PostMapping
    public ApiResponse<SystemConfigurationResponse> create(@Valid @RequestBody SystemConfigurationRequest request) {
        return ApiResponse.success("Configuration created", service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<SystemConfigurationResponse> update(@PathVariable Long id,
                                                             @Valid @RequestBody SystemConfigurationRequest request) {
        return ApiResponse.success("Configuration updated", service.update(id, request));
    }

    @GetMapping
    public ApiResponse<List<SystemConfigurationResponse>> getAll(@RequestParam(required = false) String category) {
        return ApiResponse.success(service.getAll(category));
    }

    @GetMapping("/{key}")
    public ApiResponse<SystemConfigurationResponse> getByKey(@PathVariable String key) {
        return ApiResponse.success(service.getByKey(key));
    }
}