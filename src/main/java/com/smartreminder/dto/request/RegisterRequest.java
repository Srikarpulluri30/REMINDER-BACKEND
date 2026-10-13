package com.smartreminder.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Register Request DTO
 * 
 * Captures user registration input data and enforces Bean Validation rules.
 */
public class RegisterRequest {

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Please provide a valid email format (e.g. user@example.com)")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 50, message = "Password must be at least 6 characters long")
    private String password;

    public RegisterRequest() {
    }

    public RegisterRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
