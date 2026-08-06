package com.pharmacy.pipms.exception;

public class AccountLockedException extends RuntimeException {
    public AccountLockedException(String message) { super(message); }
}