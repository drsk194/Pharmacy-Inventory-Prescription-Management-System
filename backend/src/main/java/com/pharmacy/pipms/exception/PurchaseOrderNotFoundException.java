package com.pharmacy.pipms.exception;

public class PurchaseOrderNotFoundException extends ResourceNotFoundException {
    public PurchaseOrderNotFoundException(String message) { super(message); }
}