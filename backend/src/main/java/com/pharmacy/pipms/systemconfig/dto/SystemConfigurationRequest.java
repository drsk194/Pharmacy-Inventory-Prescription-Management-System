package com.pharmacy.pipms.systemconfig.dto;

import com.pharmacy.pipms.systemconfig.entity.ConfigDataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemConfigurationRequest {
    @NotBlank(message = "Key is required")
    @Size(max = 100)
    private String configKey;

    @NotBlank(message = "Value is required")
    @Size(max = 500)
    private String configValue;

    @NotNull(message = "Data type is required")
    private ConfigDataType dataType;

    @Size(max = 50)
    private String category;

    @Size(max = 500)
    private String description;
}