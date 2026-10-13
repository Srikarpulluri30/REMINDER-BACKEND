package com.smartreminder.exception;

/**
 * Resource Not Found Exception
 * 
 * Thrown when an entity (e.g., Reminder with a given ID) cannot be found in the database.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
