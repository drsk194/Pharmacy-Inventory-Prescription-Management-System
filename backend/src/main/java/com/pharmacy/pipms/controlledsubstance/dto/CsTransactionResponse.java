package com.pharmacy.pipms.controlledsubstance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CsTransactionResponse {
    private CsRegisterEntryResponse entry;
    private List<String> warnings;
}