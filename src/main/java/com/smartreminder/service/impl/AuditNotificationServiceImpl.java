package com.smartreminder.service.impl;

import com.smartreminder.service.AuditNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Implementation of AuditNotificationService with asynchronous execution.
 */
@Service
public class AuditNotificationServiceImpl implements AuditNotificationService {

    private static final Logger log = LoggerFactory.getLogger(AuditNotificationServiceImpl.class);

    @Override
    @Async("taskExecutor")
    public void logReminderActivityAsync(String action, Long reminderId, String userEmail, String details) {
        try {
            // Simulate lightweight I/O processing (e.g. audit table insertion / dispatch)
            Thread.sleep(50);
            log.info("[ASYNC-AUDIT] Thread: {} | User: {} | Action: {} | ReminderID: {} | Details: {}",
                    Thread.currentThread().getName(), userEmail, action, reminderId, details);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[ASYNC-AUDIT] Interrupted while recording audit event: {}", e.getMessage());
        }
    }

    @Override
    @Async("taskExecutor")
    public void sendExpirationAlertNotificationAsync(String userEmail, String reminderTitle, String expiryDate) {
        log.info("[ASYNC-NOTIFICATION] Dispatching automated alert notification for '{}' (Expiry: {}) to recipient: {} [Thread: {}]",
                reminderTitle, expiryDate, userEmail, Thread.currentThread().getName());
    }
}
