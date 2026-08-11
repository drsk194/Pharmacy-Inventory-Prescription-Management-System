package com.pharmacy.pipms.exception;

public class PrescriptionNotFoundException extends ResourceNotFoundException {
    public PrescriptionNotFoundException(String message) { super(message); }
}