package com.smartreminder.controller;

import com.smartreminder.dto.request.LoginRequest;
import com.smartreminder.dto.request.RegisterRequest;
import com.smartreminder.dto.response.AuthResponse;
import com.smartreminder.dto.response.UserResponse;
import com.smartreminder.service.AuthService;
import com.smartreminder.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * Handles user registration, login (JWT generation), and current user profile retrieval.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, JWT login, and profile info")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * POST /api/auth/register
     * Public endpoint to create a new user account.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with hashed password and default ROLE_USER.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error or email already exists")
    })
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.registerUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * POST /api/auth/login
     * Public endpoint to authenticate user credentials and obtain a JWT token.
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user & get JWT token", description = "Verifies email and password, returning a signed JWT Bearer token and user info.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/auth/me
     * Protected endpoint: Requires valid Bearer JWT in Authorization header.
     */
    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user", description = "Retrieves profile information of the currently authenticated user from the Bearer JWT token.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User profile retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
