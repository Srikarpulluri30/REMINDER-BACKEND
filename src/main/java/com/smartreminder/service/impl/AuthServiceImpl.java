package com.smartreminder.service.impl;

import com.smartreminder.dto.request.LoginRequest;
import com.smartreminder.dto.response.AuthResponse;
import com.smartreminder.dto.response.UserResponse;
import com.smartreminder.entity.User;
import com.smartreminder.exception.BadRequestException;
import com.smartreminder.repository.UserRepository;
import com.smartreminder.security.JwtTokenProvider;
import com.smartreminder.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service Implementation
 * 
 * Handles user login, BCrypt password validation via AuthenticationManager,
 * and JWT token generation.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        // 1. Authenticate credentials via Spring Security AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2. Fetch User entity from database
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("User not found with email: " + normalizedEmail));

        // 3. Generate signed JWT token
        String token = tokenProvider.generateToken(user.getEmail(), user.getRole());

        // 4. Return clean AuthResponse DTO
        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                "Login successful!"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("User not found with email: " + email));

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                "User profile fetched successfully"
        );
    }
}
