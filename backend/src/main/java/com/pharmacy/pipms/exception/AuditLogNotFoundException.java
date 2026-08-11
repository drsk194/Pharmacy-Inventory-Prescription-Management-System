package com.pharmacy.pipms.exception;

public class AuditLogNotFoundException extends ResourceNotFoundException {
    public AuditLogNotFoundException(String message) { super(message); }
}