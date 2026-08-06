package com.pharmacy.pipms.exception;
// Generic "not found" exception. Module-specific exceptions
// (DrugNotFoundException, BatchNotFoundException, etc.) will extend this
// starting in Module 4, so every "not found" case maps to HTTP 404 automatically.
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

}