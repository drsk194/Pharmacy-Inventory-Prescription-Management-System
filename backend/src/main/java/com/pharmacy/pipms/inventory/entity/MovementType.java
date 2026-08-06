package com.pharmacy.pipms.inventory.entity;

// RECEIPT and ADJUSTMENT are used starting this module.
// DISPENSING, RETURN, TRANSFER, QUARANTINE, DISPOSAL are reserved for
// Modules 9, 11, 12, and 14, which will write movements of those types
// into this same table.
public enum MovementType {
    RECEIPT, DISPENSING, RETURN, ADJUSTMENT, TRANSFER, QUARANTINE, DISPOSAL
}