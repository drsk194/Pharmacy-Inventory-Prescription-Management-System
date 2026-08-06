package com.pharmacy.pipms.drug.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Public/guest view — deliberately excludes reorderLevel, minStockLevel,
// maxStockLevel, and barcode. Per SRS Section 4: guest access to the drug
// catalog is allowed, but stock/operational data must not be exposed.
@Getter
@AllArgsConstructor
public class DrugCatalogResponse {
    private Long id;
    private String genericName;
    private String brandName;
    private String drugClass;
    private String schedule;
    private String storageCondition;
    private String unitOfMeasure;
}