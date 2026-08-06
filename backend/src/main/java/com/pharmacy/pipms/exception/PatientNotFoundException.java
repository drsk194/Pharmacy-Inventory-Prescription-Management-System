package com.pharmacy.pipms.exception;

public class PatientNotFoundException extends ResourceNotFoundException {
    public PatientNotFoundException(String message) { super(message); }
}