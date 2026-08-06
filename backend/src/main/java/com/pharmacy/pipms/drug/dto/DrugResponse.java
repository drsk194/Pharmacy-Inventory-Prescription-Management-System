package com.pharmacy.pipms.drug.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Full internal view — staff only. Includes operational fields
// (reorder/min/max stock) that DrugCatalogResponse deliberately omits.
@Getter
@AllArgsConstructor
public class DrugResponse {
    private Long id;
    private String genericName;
    private String brandName;
    private String ndcCode;
    private String drugClass;
    private String schedule;
    private String storageCondition;
    private String unitOfMeasure;
    private Integer reorderLevel;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private String barcode;
    private boolean active;
}