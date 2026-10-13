package com.smartreminder.repository;

import com.smartreminder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository
 * 
 * Provides database operations for the User entity via Spring Data JPA.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their unique email address.
     * Useful for login authentication and profile fetching.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check whether an email already exists in the database.
     * Useful for registration validation.
     */
    boolean existsByEmail(String email);
}
