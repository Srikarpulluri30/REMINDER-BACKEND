package com.smartreminder.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health Check Controller
 * 
 * Provides a simple endpoint to verify that the backend server is running
 * and accessible from the browser, Postman, or frontend application.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("application", "Smart Reminder Backend");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "Smart Reminder API foundation is running successfully!");

        return ResponseEntity.ok(response);
    }
}
