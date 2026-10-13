package com.smartreminder.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Login Request DTO
 * 
 * Captures user login credentials for authentication.
 */
public class LoginRequest {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Please provide a valid email format (e.g. user@example.com)")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
