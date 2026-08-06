package com.pharmacy.pipms.inventory.dto;

import com.pharmacy.pipms.inventory.entity.LocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationCreateRequest {
    @NotBlank(message = "Location name is required")
    @Size(max = 100)
    private String name;

    @NotNull(message = "Location type is required")
    private LocationType type;

    @Size(max = 255)
    private String description;
}