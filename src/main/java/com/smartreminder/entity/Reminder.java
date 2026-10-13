package com.smartreminder.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Reminder Entity
 * 
 * Represents an individual document or task expiry reminder.
 * Maps to the 'reminders' table in MySQL.
 */
@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String category; // e.g. PASSPORT, LICENCE, INSURANCE, SUBSCRIPTION, OTHER

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "reminder_days", nullable = false)
    private Integer reminderDays; // Number of days before expiry (e.g. 15)

    @Column(name = "reminder_date", nullable = false)
    private LocalDate reminderDate; // Calculated alert date: expiryDate - reminderDays

    @Column(nullable = false, length = 20)
    private String status = "UPCOMING"; // UPCOMING, DUE_SOON, EXPIRED, COMPLETED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Reminder() {
    }

    public Reminder(Long id, String title, String description, String category, LocalDate expiryDate,
                    Integer reminderDays, LocalDate reminderDate, String status, LocalDateTime createdAt,
                    LocalDateTime updatedAt, User user) {
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
        this.user = user;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null || this.status.trim().isEmpty()) {
            this.status = "UPCOMING";
        }
        if (this.expiryDate != null && this.reminderDays != null && this.reminderDate == null) {
            this.reminderDate = this.expiryDate.minusDays(this.reminderDays);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.expiryDate != null && this.reminderDays != null) {
            this.reminderDate = this.expiryDate.minusDays(this.reminderDays);
        }
    }

    // Getters and Setters
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
