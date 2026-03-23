package com.clara.ops.challenge.document_management_service_challenge.controller.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Used to contain information of a new user registration.
 *
 * @param username The username of the new user. Should only contain letters (either lowercase or
 *     uppercase are allowed) and numbers
 * @param password The password of the new user.
 * @param fullName The full name of the new user.
 */
public record UserData(
    @Size(min = 5, max = 50, message = "Username should contain between 5 and 25 characters")
        @Pattern(
            regexp = "^[a-zA-Z0-9]*$",
            message = "Username contains invalid characters. Only letters and numbers are allowed.")
        String username,
    @Size(min = 8, max = 50, message = "Password should contain between 8 and 25 characters")
        String password,
    @Size(min = 10, max = 50, message = "Full name should contain between 10 and 100 characters")
        String fullName) {}
