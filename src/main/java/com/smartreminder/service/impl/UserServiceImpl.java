package com.smartreminder.service.impl;

import com.smartreminder.dto.request.RegisterRequest;
import com.smartreminder.dto.response.UserResponse;
import com.smartreminder.entity.User;
import com.smartreminder.exception.BadRequestException;
import com.smartreminder.repository.UserRepository;
import com.smartreminder.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Service Implementation
 * 
 * Handles user business logic:
 * 1. Email uniqueness verification
 * 2. BCrypt password hashing
 * 3. Persistence to MySQL via UserRepository
 * 4. Conversion to UserResponse DTO
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        // 1. Business Validation: Check if email already exists
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");
        }

        // 2. Hash raw password using BCrypt
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 3. Construct User entity
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(hashedPassword);
        user.setRole("ROLE_USER");

        // 4. Save to MySQL database
        User savedUser = userRepository.save(user);

        // 5. Map to UserResponse DTO (Never include password!)
        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getCreatedAt(),
                "User registered successfully!"
        );
    }
}
