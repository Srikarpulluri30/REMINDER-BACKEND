package com.smartreminder.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global Exception Handler
 * 
 * Intercepts validation errors, bad credentials, resource not found, access denied,
 * and business exceptions across the application and formats them into clean, structured JSON responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle Jakarta Bean Validation errors (@NotBlank, @Email, @Size, @NotNull, @Min)
     * Returns HTTP 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("timestamp", LocalDateTime.now().toString());
        errorBody.put("status", HttpStatus.BAD_REQUEST.value());
        errorBody.put("error", "Validation Error");

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        errorBody.put("validationErrors", fieldErrors);
        errorBody.put("message", "Input validation failed. Please check the fields.");

        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle ResourceNotFoundException (e.g. Reminder ID not found or belongs to another user)
     * Returns HTTP 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("timestamp", LocalDateTime.now().toString());
        errorBody.put("status", HttpStatus.NOT_FOUND.value());
        errorBody.put("error", "Not Found");
        errorBody.put("message", ex.getMessage());

        return new ResponseEntity<>(errorBody, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle BadCredentialsException and general AuthenticationException
     * Returns HTTP 401 Unauthorized
     */
    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(Exception ex) {
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("timestamp", LocalDateTime.now().toString());
        errorBody.put("status", HttpStatus.UNAUTHORIZED.value());
        errorBody.put("error", "Unauthorized");
        errorBody.put("message", "Invalid email or password. Please verify your credentials.");

        return new ResponseEntity<>(errorBody, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle AccessDeniedException (e.g. User attempting forbidden action)
     * Returns HTTP 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("timestamp", LocalDateTime.now().toString());
        errorBody.put("status", HttpStatus.FORBIDDEN.value());
        errorBody.put("error", "Forbidden");
        errorBody.put("message", "Access Denied: You do not have permission to access this resource.");

        return new ResponseEntity<>(errorBody, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle business logic bad requests (e.g. Email already registered)
     * Returns HTTP 400 Bad Request
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException ex) {
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("timestamp", LocalDateTime.now().toString());
        errorBody.put("status", HttpStatus.BAD_REQUEST.value());
        errorBody.put("error", "Bad Request");
        errorBody.put("message", ex.getMessage());

        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalArgumentException
     * Returns HTTP 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("timestamp", LocalDateTime.now().toString());
        errorBody.put("status", HttpStatus.BAD_REQUEST.value());
        errorBody.put("error", "Bad Request");
        errorBody.put("message", ex.getMessage());

        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle generic unexpected errors
     * Returns HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("timestamp", LocalDateTime.now().toString());
        errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorBody.put("error", "Internal Server Error");
        errorBody.put("message", ex.getMessage() != null ? ex.getMessage() : "An unexpected server error occurred.");

        return new ResponseEntity<>(errorBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
