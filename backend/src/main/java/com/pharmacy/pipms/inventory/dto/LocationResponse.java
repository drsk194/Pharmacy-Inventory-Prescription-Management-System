package com.pharmacy.pipms.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LocationResponse {
    private Long id;
    private String name;
    private String type;
    private String description;
    private boolean active;
}