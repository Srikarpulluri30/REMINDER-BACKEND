package com.smartreminder.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Reminder Request DTO
 * 
 * Captures user input when creating or updating a reminder.
 */
public class ReminderRequest {

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;

    private String description;

    @NotBlank(message = "Category cannot be blank (e.g. PASSPORT, LICENCE, INSURANCE, SUBSCRIPTION, OTHER)")
    @Size(max = 50, message = "Category name must not exceed 50 characters")
    private String category;

    @NotNull(message = "Expiry date is required (format: YYYY-MM-DD)")
    private LocalDate expiryDate;

    @NotNull(message = "Reminder days before expiry is required")
    @Min(value = 0, message = "Reminder days cannot be negative")
    private Integer reminderDays;

    public ReminderRequest() {
    }

    public ReminderRequest(String title, String description, String category, LocalDate expiryDate, Integer reminderDays) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.expiryDate = expiryDate;
        this.reminderDays = reminderDays;
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
}
