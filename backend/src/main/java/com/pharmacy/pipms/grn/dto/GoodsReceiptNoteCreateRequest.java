package com.pharmacy.pipms.grn.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class GoodsReceiptNoteCreateRequest {

    @NotNull(message = "Purchase order ID is required")
    private Long purchaseOrderId;

    @NotNull(message = "Received date is required")
    @PastOrPresent(message = "Received date cannot be in the future")
    private LocalDateTime receivedDate;

    // Required only if receivedDate is more than 48 hours before now, and
    // only usable by an Admin's token — see module notes, Assumption 2.
    private String lateReceiptOverrideReason;

    @Size(max = 1000)
    private String generalNotes;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<GoodsReceiptItemRequest> items;
}