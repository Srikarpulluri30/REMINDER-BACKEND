package com.smartreminder.exception;

/**
 * Custom Bad Request Exception
 * 
 * Thrown when business validation fails (e.g., email already registered).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
