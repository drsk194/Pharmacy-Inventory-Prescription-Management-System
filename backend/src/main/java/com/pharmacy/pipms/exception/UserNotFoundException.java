package com.pharmacy.pipms.exception;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(String message) { super(message); }
}