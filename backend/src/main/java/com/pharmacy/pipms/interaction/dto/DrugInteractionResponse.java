package com.pharmacy.pipms.interaction.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DrugInteractionResponse {
    private Long id;
    private Long drugAId;
    private String drugAName;
    private Long drugBId;
    private String drugBName;
    private String severity;
    private String description;
}