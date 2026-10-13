package com.smartreminder.service;

/**
 * Asynchronous Audit & Notification Service Interface
 * 
 * Genuine @Async Use Case:
 * Offloads audit logging, notification dispatch, and background telemetry
 * to an asynchronous worker thread pool.
 * Prevents I/O blocking on the main HTTP request thread, improving API latency.
 */
public interface AuditNotificationService {

    /**
     * Non-blocking background audit logger for reminder events.
     */
    void logReminderActivityAsync(String action, Long reminderId, String userEmail, String details);

    /**
     * Non-blocking background notification simulator for upcoming expiration alerts.
     */
    void sendExpirationAlertNotificationAsync(String userEmail, String reminderTitle, String expiryDate);
}
