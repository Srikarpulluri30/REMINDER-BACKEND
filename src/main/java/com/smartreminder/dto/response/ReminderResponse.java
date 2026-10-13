package com.smartreminder.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Reminder Response DTO
 * 
 * Returned to the client containing complete reminder details, calculated reminderDate,
 * current status, and associated user identifiers.
 */
public class ReminderResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private LocalDate expiryDate;
    private Integer reminderDays;
    private LocalDate reminderDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
    private String userEmail;

    public ReminderResponse() {
    }

    public ReminderResponse(Long id, String title, String description, String category,
                            LocalDate expiryDate, Integer reminderDays, LocalDate reminderDate,
                            String status, LocalDateTime createdAt, LocalDateTime updatedAt,
                            Long userId, String userEmail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.expiryDate = expiryDate;
        this.reminderDays = reminderDays;
        this.reminderDate = reminderDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userId = userId;
        this.userEmail = userEmail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getReminderDays() {
        return reminderDays;
    }

    public void setReminderDays(Integer reminderDays) {
        this.reminderDays = reminderDays;
    }

    public LocalDate getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(LocalDate reminderDate) {
        this.reminderDate = reminderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
