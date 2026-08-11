package com.pharmacy.pipms.controlledsubstance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class IntegrityCheckResponse {
    private boolean intact;
    private int totalEntriesChecked;
    private List<Long> tamperedEntryIds;
}