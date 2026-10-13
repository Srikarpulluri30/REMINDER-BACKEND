package com.smartreminder.controller;

import com.smartreminder.dto.response.UserResponse;
import com.smartreminder.entity.User;
import com.smartreminder.repository.ReminderRepository;
import com.smartreminder.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin REST Controller
 * 
 * Provides administrative endpoints restricted strictly to users with ROLE_ADMIN.
 * Enforces Role-Based Access Control (RBAC) via @PreAuthorize and Spring Security URL rules.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Endpoints restricted exclusively to users with ROLE_ADMIN")
public class AdminController {

    private final UserRepository userRepository;
    private final ReminderRepository reminderRepository;

    public AdminController(UserRepository userRepository, ReminderRepository reminderRepository) {
        this.userRepository = userRepository;
        this.reminderRepository = reminderRepository;
    }

    /**
     * GET /api/admin/users
     * Retrieves all registered users in the system (ADMIN only).
     */
    @GetMapping("/users")
    @Operation(summary = "Get all registered users (Admin only)", description = "Returns a list of all user accounts registered in the database. Requires ROLE_ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of users retrieved"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ROLE_ADMIN")
    })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> response = users.stream()
                .map(u -> new UserResponse(
                        u.getId(),
                        u.getName(),
                        u.getEmail(),
                        u.getRole(),
                        u.getCreatedAt(),
                        "Active user"
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/stats
     * Retrieves global system metrics for the administrative dashboard.
     * Caches query result in memory to avoid repetitive database aggregate COUNT queries.
     */
    @GetMapping("/stats")
    @Cacheable(value = "adminStats", key = "'global'")
    @Operation(summary = "Get system statistics (Admin only)", description = "Returns system-wide metrics (total users, total reminders). Requires ROLE_ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "System metrics retrieved"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ROLE_ADMIN")
    })
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        long totalUsers = userRepository.count();
        long totalReminders = reminderRepository.count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalReminders", totalReminders);
        stats.put("serverStatus", "ONLINE");

        return ResponseEntity.ok(stats);
    }
}
