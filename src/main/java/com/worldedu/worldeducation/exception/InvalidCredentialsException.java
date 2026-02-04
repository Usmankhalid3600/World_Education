package com.worldedu.worldeducation.exception;

public class InvalidCredentialsException extends AuthenticationException {
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
