package com.pharmacy.pipms.grn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GoodsReceiptNoteResponse {
    private Long id;
    private Long purchaseOrderId;
    private LocalDateTime receivedDate;
    private String receivedByName;
    private int totalItems;
    private String discrepancyNotes;
    private String generalNotes;
    private LocalDateTime createdAt;
    private List<GoodsReceiptItemResponse> items;
}