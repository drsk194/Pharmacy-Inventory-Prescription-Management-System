package com.pharmacy.pipms.exception;

public class ExpiredDrugException extends RuntimeException {
    public ExpiredDrugException(String message) { super(message); }
}