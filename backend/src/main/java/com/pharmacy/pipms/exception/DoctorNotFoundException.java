package com.pharmacy.pipms.exception;

public class DoctorNotFoundException extends ResourceNotFoundException {
    public DoctorNotFoundException(String message) { super(message); }
}