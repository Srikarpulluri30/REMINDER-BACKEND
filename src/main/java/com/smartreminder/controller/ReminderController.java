package com.smartreminder.controller;

import com.smartreminder.dto.request.ReminderRequest;
import com.smartreminder.dto.response.PagedResponse;
import com.smartreminder.dto.response.ReminderResponse;
import com.smartreminder.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reminder REST Controller
 * 
 * Exposes CRUD endpoints, pagination, sorting, and lifecycle query endpoints for reminders.
 * All endpoints require a valid JWT Bearer token and enforce user data isolation.
 */
@RestController
@RequestMapping("/api/reminders")
@Tag(name = "Reminders", description = "Endpoints for managing expiration reminders, filtering, and lifecycle status")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /**
     * POST /api/reminders
     */
    @PostMapping
    @Operation(summary = "Create a new reminder", description = "Creates a new expiration reminder, automatically calculating reminderDate = expiryDate - reminderDays and initial status.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Reminder created successfully"),
        @ApiResponse(responseCode = "400", description = "Bean validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ReminderResponse> createReminder(
            @Valid @RequestBody ReminderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReminderResponse response = reminderService.createReminder(request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * GET /api/reminders
     */
    @GetMapping
    @Operation(summary = "Get paginated & sorted reminders", description = "Retrieves user's reminders with pagination, sorting by expiryDate/reminderDate/title, and filtering by category/status/search.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paginated reminders retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<PagedResponse<ReminderResponse>> getAllReminders(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field: expiryDate, reminderDate, title, createdAt") @RequestParam(defaultValue = "expiryDate") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Category filter: PASSPORT, LICENCE, INSURANCE, SUBSCRIPTION, VEHICLE, OTHER") @RequestParam(required = false) String category,
            @Parameter(description = "Status filter: UPCOMING, DUE_SOON, EXPIRED, COMPLETED") @RequestParam(required = false) String status,
            @Parameter(description = "Search keyword in title or description") @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserDetails userDetails) {
        PagedResponse<ReminderResponse> pagedResponse = reminderService.getAllUserRemindersPaged(
                userDetails.getUsername(),
                category,
                status,
                search,
                page,
                size,
                sortBy,
                sortDir
        );
        return ResponseEntity.ok(pagedResponse);
    }

    /**
     * GET /api/reminders/all
     */
    @GetMapping("/all")
    @Operation(summary = "Get all reminders unpaged", description = "Returns all reminders for the authenticated user without pagination (useful for dashboard stats).")
    public ResponseEntity<List<ReminderResponse>> getAllRemindersUnpaged(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ReminderResponse> reminders = reminderService.getAllUserReminders(
                userDetails.getUsername(), null, null, null
        );
        return ResponseEntity.ok(reminders);
    }

    /**
     * GET /api/reminders/upcoming
     */
    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming reminders", description = "Retrieves reminders with UPCOMING status (today < reminderDate).")
    public ResponseEntity<List<ReminderResponse>> getUpcomingReminders(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ReminderResponse> reminders = reminderService.getUpcomingReminders(userDetails.getUsername());
        return ResponseEntity.ok(reminders);
    }

    /**
     * GET /api/reminders/due-soon
     */
    @GetMapping("/due-soon")
    @Operation(summary = "Get due-soon reminders", description = "Retrieves reminders in the active warning window (today >= reminderDate && today <= expiryDate).")
    public ResponseEntity<List<ReminderResponse>> getDueSoonReminders(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ReminderResponse> reminders = reminderService.getDueSoonReminders(userDetails.getUsername());
        return ResponseEntity.ok(reminders);
    }

    /**
     * GET /api/reminders/expired
     */
    @GetMapping("/expired")
    @Operation(summary = "Get expired reminders", description = "Retrieves reminders past their expiration deadline (today > expiryDate).")
    public ResponseEntity<List<ReminderResponse>> getExpiredReminders(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ReminderResponse> reminders = reminderService.getExpiredReminders(userDetails.getUsername());
        return ResponseEntity.ok(reminders);
    }

    /**
     * GET /api/reminders/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get reminder by ID", description = "Retrieves a specific reminder by ID, verifying that it belongs to the authenticated user.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reminder found"),
        @ApiResponse(responseCode = "404", description = "Reminder not found or unauthorized access")
    })
    public ResponseEntity<ReminderResponse> getReminderById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReminderResponse response = reminderService.getReminderById(id, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/reminders/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update reminder by ID", description = "Updates an existing reminder, recalculating reminderDate and lifecycle status.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reminder updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Reminder not found")
    })
    public ResponseEntity<ReminderResponse> updateReminder(
            @PathVariable Long id,
            @Valid @RequestBody ReminderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReminderResponse response = reminderService.updateReminder(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/reminders/{id}/complete
     */
    @PatchMapping("/{id}/complete")
    @Operation(summary = "Mark reminder as completed", description = "Updates reminder status to COMPLETED.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reminder marked completed"),
        @ApiResponse(responseCode = "404", description = "Reminder not found")
    })
    public ResponseEntity<ReminderResponse> markAsCompleted(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReminderResponse response = reminderService.markAsCompleted(id, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/reminders/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete reminder by ID", description = "Permanently deletes a reminder, verifying ownership.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reminder deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Reminder not found")
    })
    public ResponseEntity<Map<String, Object>> deleteReminder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        reminderService.deleteReminder(id, userDetails.getUsername());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Reminder with ID " + id + " was deleted successfully.");
        response.put("id", id);

        return ResponseEntity.ok(response);
    }
}
