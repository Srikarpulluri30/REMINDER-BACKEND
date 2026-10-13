package com.smartreminder.security;

import com.smartreminder.entity.User;
import com.smartreminder.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom User Details Service
 * 
 * Loads user records from MySQL database for Spring Security authentication
 * and ensures role authorities adhere to standard Spring Security prefixes (ROLE_).
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String role = user.getRole();
        if (role == null || role.trim().isEmpty()) {
            role = "ROLE_USER";
        } else if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role.trim().toUpperCase();
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}
