package com.pharmacy.pipms.exception;

public class BillNotFoundException extends ResourceNotFoundException {
    public BillNotFoundException(String message) { super(message); }
}