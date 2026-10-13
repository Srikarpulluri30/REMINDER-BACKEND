package com.smartreminder.service;

import com.smartreminder.dto.request.RegisterRequest;
import com.smartreminder.dto.response.UserResponse;

/**
 * User Service Interface
 */
public interface UserService {

    /**
     * Register a new user with BCrypt password hashing.
     * 
     * @param request The registration request containing name, email, and password.
     * @return UserResponse containing the created user details (without password).
     */
    UserResponse registerUser(RegisterRequest request);
}
