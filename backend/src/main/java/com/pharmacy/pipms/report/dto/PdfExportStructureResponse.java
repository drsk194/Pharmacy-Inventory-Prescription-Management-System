package com.pharmacy.pipms.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// "PDF export structure" per FR11 — the metadata a real PDF template would
// consume. Binary rendering isn't implemented; see Module 17's Assumption 7.
@Getter
@AllArgsConstructor
public class PdfExportStructureResponse {
    private String reportTitle;
    private String generatedBy;
    private LocalDateTime generatedAt;
    private String filtersApplied;
    private int rowCount;
    private String note;
}