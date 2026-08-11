package com.pharmacy.pipms.systemconfig.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SystemConfigurationResponse {
    private Long id;
    private String configKey;
    private String configValue;
    private String dataType;
    private String category;
    private String description;
}