package com.pharmacy.pipms.exception;

public class DualAuthorizationRequiredException extends RuntimeException {
    public DualAuthorizationRequiredException(String message) { super(message); }
}