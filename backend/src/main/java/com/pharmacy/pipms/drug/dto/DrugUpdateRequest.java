package com.pharmacy.pipms.drug.dto;

import com.pharmacy.pipms.drug.entity.DrugSchedule;
import com.pharmacy.pipms.drug.entity.StorageCondition;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrugUpdateRequest {

    @NotBlank(message = "Generic name is required")
    @Pattern(regexp = "^[A-Za-z ]{2,200}$", message = "Generic name must be alphabetic and spaces only, 2-200 characters")
    private String genericName;

    @Size(max = 200)
    private String brandName;

    @Pattern(regexp = "^\\d{4,5}-\\d{3,4}-\\d{1,2}$", message = "Invalid NDC code format (expected e.g. 12345-6789-01)")
    private String ndcCode;

    @NotBlank(message = "Drug class is required")
    @Size(max = 100)
    private String drugClass;

    @NotNull(message = "Schedule classification is required")
    private DrugSchedule schedule;

    @NotNull(message = "Storage condition is required")
    private StorageCondition storageCondition;

    @NotBlank(message = "Unit of measure is required")
    @Size(max = 20)
    private String unitOfMeasure;

    @NotNull(message = "Reorder level is required")
    @Min(value = 0, message = "Reorder level must be non-negative")
    private Integer reorderLevel;

    @NotNull(message = "Minimum stock level is required")
    @Min(value = 0, message = "Minimum stock level must be non-negative")
    private Integer minStockLevel;

    @Min(value = 0, message = "Maximum stock level must be non-negative")
    private Integer maxStockLevel;

    @Size(max = 100)
    private String barcode;
    private Integer maxPrescriptionQtyPerFill;
    private Integer maxRefillsAllowed;
}