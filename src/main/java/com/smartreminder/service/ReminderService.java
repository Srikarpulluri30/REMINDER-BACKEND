package com.smartreminder.service;

import com.smartreminder.dto.request.ReminderRequest;
import com.smartreminder.dto.response.PagedResponse;
import com.smartreminder.dto.response.ReminderResponse;

import java.util.List;

/**
 * Reminder Service Interface
 * 
 * Defines CRUD business operations, lifecycle status calculation,
 * pagination, sorting, and user data isolation for Reminders.
 */
public interface ReminderService {

    /**
     * Create a new reminder associated with the authenticated user.
     */
    ReminderResponse createReminder(ReminderRequest request, String userEmail);

    /**
     * Get paginated and sorted reminders for the authenticated user with optional filtering and search.
     */
    PagedResponse<ReminderResponse> getAllUserRemindersPaged(
            String userEmail,
            String category,
            String status,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    /**
     * Get all reminders for the authenticated user without pagination (e.g. for dashboard metrics).
     */
    List<ReminderResponse> getAllUserReminders(String userEmail, String category, String status, String search);

    /**
     * Get a specific reminder by ID, verifying ownership.
     */
    ReminderResponse getReminderById(Long id, String userEmail);

    /**
     * Update an existing reminder, recalculating reminderDate and verifying ownership.
     */
    ReminderResponse updateReminder(Long id, ReminderRequest request, String userEmail);

    /**
     * Delete a reminder by ID, verifying ownership.
     */
    void deleteReminder(Long id, String userEmail);

    /**
     * Get all reminders with UPCOMING status for the authenticated user.
     */
    List<ReminderResponse> getUpcomingReminders(String userEmail);

    /**
     * Get all reminders with DUE_SOON status for the authenticated user.
     */
    List<ReminderResponse> getDueSoonReminders(String userEmail);

    /**
     * Get all reminders with EXPIRED status for the authenticated user.
     */
    List<ReminderResponse> getExpiredReminders(String userEmail);

    /**
     * Mark a specific reminder as COMPLETED for the authenticated user.
     */
    ReminderResponse markAsCompleted(Long id, String userEmail);
}
