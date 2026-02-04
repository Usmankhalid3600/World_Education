package com.worldedu.worldeducation.exception;

public class AccountLockedException extends AuthenticationException {
    
    public AccountLockedException(String message) {
        super(message);
    }
}
