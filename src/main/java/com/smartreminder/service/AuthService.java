package com.smartreminder.service;

import com.smartreminder.dto.request.LoginRequest;
import com.smartreminder.dto.response.AuthResponse;
import com.smartreminder.dto.response.UserResponse;

/**
 * Authentication Service Interface
 */
public interface AuthService {

    /**
     * Authenticate user credentials and return a signed JWT token.
     * 
     * @param request Login credentials (email, password).
     * @return AuthResponse containing token and user profile summary.
     */
    AuthResponse login(LoginRequest request);

    /**
     * Get profile of the currently authenticated user by email.
     * 
     * @param email The authenticated user's email.
     * @return UserResponse without password.
     */
    UserResponse getCurrentUser(String email);
}
