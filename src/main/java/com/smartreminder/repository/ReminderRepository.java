package com.smartreminder.repository;

import com.smartreminder.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Reminder Repository
 * 
 * Provides database operations and custom query methods for the Reminder entity.
 */
@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    /**
     * Find all reminders belonging to a specific user.
     */
    List<Reminder> findByUserId(Long userId);

    /**
     * Find all reminders for a specific user filtered by status (ACTIVE, DUE_SOON, EXPIRED, COMPLETED).
     */
    List<Reminder> findByUserIdAndStatus(Long userId, String status);

    /**
     * Find all reminders for a user filtered by category (e.g. PASSPORT, LICENCE, INSURANCE).
     */
    List<Reminder> findByUserIdAndCategory(Long userId, String category);

    /**
     * Search reminders by title containing a keyword (case-insensitive) for a specific user.
     */
    List<Reminder> findByUserIdAndTitleContainingIgnoreCase(Long userId, String keyword);

    /**
     * Find a specific reminder ensuring it belongs to the authenticated user.
     */
    Optional<Reminder> findByIdAndUserId(Long id, Long userId);

    /**
     * Count reminders by status for user dashboard statistics.
     */
    long countByUserIdAndStatus(Long userId, String status);

    /**
     * Total count of reminders for a user.
     */
    long countByUserId(Long userId);

    /**
     * Find reminders where reminderDate is on or before a given date and status is ACTIVE.
     * Useful for automated scheduled status update jobs.
     */
    List<Reminder> findByStatusAndReminderDateLessThanEqual(String status, LocalDate date);
}
