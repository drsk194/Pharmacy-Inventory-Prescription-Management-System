package com.pharmacy.pipms.exception;

public class InvalidPrescriptionStatusException extends RuntimeException {
    public InvalidPrescriptionStatusException(String message) { super(message); }
}