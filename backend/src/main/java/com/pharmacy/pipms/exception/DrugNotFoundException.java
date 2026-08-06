package com.pharmacy.pipms.exception;

public class DrugNotFoundException extends ResourceNotFoundException {
    public DrugNotFoundException(String message) { super(message); }
}