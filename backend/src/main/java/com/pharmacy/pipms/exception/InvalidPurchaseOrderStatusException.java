package com.pharmacy.pipms.exception;

public class InvalidPurchaseOrderStatusException extends RuntimeException {
    public InvalidPurchaseOrderStatusException(String message) { super(message); }
}