package com.smartreminder.service;

import com.smartreminder.dto.request.ReminderRequest;
import com.smartreminder.dto.response.ReminderResponse;
import com.smartreminder.entity.Reminder;
import com.smartreminder.entity.User;
import com.smartreminder.exception.ResourceNotFoundException;
import com.smartreminder.repository.ReminderRepository;
import com.smartreminder.repository.UserRepository;
import com.smartreminder.service.impl.ReminderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for ReminderService (JUnit 5 + Mockito)
 * 
 * Verifies core business logic:
 * 1. Automatic reminderDate calculation (expiryDate - reminderDays)
 * 2. Deterministic lifecycle status evaluation (UPCOMING, DUE_SOON, EXPIRED, COMPLETED)
 * 3. User data isolation & security ownership checks
 * 4. Error scenarios (User not found, Reminder not found)
 */
@ExtendWith(MockitoExtension.class)
public class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditNotificationService auditNotificationService;

    @InjectMocks
    private ReminderServiceImpl reminderService;

    private User testUser;
    private Reminder testReminder;
    private ReminderRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "John Doe", "john.doe@example.com", "hashedPassword", "ROLE_USER", LocalDateTime.now());

        LocalDate expiry = LocalDate.now().plusDays(20);
        int reminderDays = 5;
        LocalDate reminderDate = expiry.minusDays(reminderDays);

        testReminder = new Reminder(
                100L,
                "Driving Licence Renewal",
                "Renew driving licence online",
                "LICENCE",
                expiry,
                reminderDays,
                reminderDate,
                "UPCOMING",
                LocalDateTime.now(),
                LocalDateTime.now(),
                testUser
        );

        testRequest = new ReminderRequest(
                "Driving Licence Renewal",
                "Renew driving licence online",
                "LICENCE",
                expiry,
                reminderDays
        );
    }

    @Test
    @DisplayName("Test 1: createReminder() accurately calculates reminderDate = expiryDate - reminderDays")
    void testCreateReminder_CalculatesReminderDateCorrectly() {
        // Arrange
        LocalDate expiry = LocalDate.of(2026, 12, 25);
        int daysBefore = 10;
        LocalDate expectedAlertDate = LocalDate.of(2026, 12, 15);

        testRequest.setExpiryDate(expiry);
        testRequest.setReminderDays(daysBefore);

        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));
        when(reminderRepository.save(any(Reminder.class))).thenAnswer(invocation -> {
            Reminder saved = invocation.getArgument(0);
            saved.setId(101L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        // Act
        ReminderResponse response = reminderService.createReminder(testRequest, "john.doe@example.com");

        // Assert
        assertNotNull(response);
        assertEquals(expectedAlertDate, response.getReminderDate(), "Reminder alert date must equal expiryDate - reminderDays");
        assertEquals("Driving Licence Renewal", response.getTitle());
        assertEquals("LICENCE", response.getCategory());
        verify(reminderRepository, times(1)).save(any(Reminder.class));
    }

    @Test
    @DisplayName("Test 2: evaluateStatus() returns UPCOMING when today < reminderDate")
    void testEvaluateStatus_Upcoming() {
        LocalDate expiryDate = LocalDate.now().plusDays(30);
        LocalDate reminderDate = LocalDate.now().plusDays(10);

        String status = reminderService.evaluateStatus(expiryDate, reminderDate, "UPCOMING");

        assertEquals("UPCOMING", status, "Status should be UPCOMING when current date has not reached reminder alert date.");
    }

    @Test
    @DisplayName("Test 3: evaluateStatus() returns DUE_SOON when today is within the alert window")
    void testEvaluateStatus_DueSoon() {
        LocalDate expiryDate = LocalDate.now().plusDays(5);
        LocalDate reminderDate = LocalDate.now().minusDays(2); // Passed 2 days ago

        String status = reminderService.evaluateStatus(expiryDate, reminderDate, "UPCOMING");

        assertEquals("DUE_SOON", status, "Status should be DUE_SOON when current date is between reminderDate and expiryDate.");
    }

    @Test
    @DisplayName("Test 4: evaluateStatus() returns EXPIRED when today > expiryDate")
    void testEvaluateStatus_Expired() {
        LocalDate expiryDate = LocalDate.now().minusDays(1); // Expired yesterday
        LocalDate reminderDate = LocalDate.now().minusDays(10);

        String status = reminderService.evaluateStatus(expiryDate, reminderDate, "UPCOMING");

        assertEquals("EXPIRED", status, "Status should be EXPIRED when expiration date has passed.");
    }

    @Test
    @DisplayName("Test 5: evaluateStatus() preserves COMPLETED status regardless of date")
    void testEvaluateStatus_PreservesCompleted() {
        LocalDate expiryDate = LocalDate.now().minusDays(10);
        LocalDate reminderDate = LocalDate.now().minusDays(20);

        String status = reminderService.evaluateStatus(expiryDate, reminderDate, "COMPLETED");

        assertEquals("COMPLETED", status, "Status must remain COMPLETED once marked completed by user.");
    }

    @Test
    @DisplayName("Test 6: getReminderById() returns reminder when user owns the resource")
    void testGetReminderById_UserOwnershipSuccess() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));
        when(reminderRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(testReminder));

        ReminderResponse response = reminderService.getReminderById(100L, "john.doe@example.com");

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Driving Licence Renewal", response.getTitle());
    }

    @Test
    @DisplayName("Test 7: getReminderById() throws ResourceNotFoundException when reminder does not belong to user")
    void testGetReminderById_ThrowsResourceNotFound_WhenUserDoesNotOwnReminder() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));
        when(reminderRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            reminderService.getReminderById(999L, "john.doe@example.com");
        });
    }

    @Test
    @DisplayName("Test 8: updateReminder() recalculates reminderDate and saves successfully")
    void testUpdateReminder_RecalculatesDateAndStatus() {
        LocalDate newExpiry = LocalDate.of(2027, 1, 15);
        int newReminderDays = 20;
        LocalDate expectedNewAlertDate = LocalDate.of(2026, 12, 26);

        ReminderRequest updateRequest = new ReminderRequest(
                "Updated Licence Title",
                "New description notes",
                "LICENCE",
                newExpiry,
                newReminderDays
        );

        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));
        when(reminderRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(testReminder));
        when(reminderRepository.save(any(Reminder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReminderResponse response = reminderService.updateReminder(100L, updateRequest, "john.doe@example.com");

        assertNotNull(response);
        assertEquals("Updated Licence Title", response.getTitle());
        assertEquals(expectedNewAlertDate, response.getReminderDate());
    }

    @Test
    @DisplayName("Test 9: deleteReminder() calls repository delete when user owns reminder")
    void testDeleteReminder_Success() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));
        when(reminderRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(testReminder));
        doNothing().when(reminderRepository).delete(testReminder);

        assertDoesNotThrow(() -> reminderService.deleteReminder(100L, "john.doe@example.com"));

        verify(reminderRepository, times(1)).delete(testReminder);
    }

    @Test
    @DisplayName("Test 10: createReminder() throws ResourceNotFoundException when user email does not exist")
    void testCreateReminder_ThrowsException_WhenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            reminderService.createReminder(testRequest, "unknown@example.com");
        });
    }
}
