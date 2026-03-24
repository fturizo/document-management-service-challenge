package com.clara.ops.challenge.document_management_service_challenge.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * Used to hold information on the document data provided during an upload operation.
 *
 * @param name The name of the document
 * @param tags A set of tags attached to the document
 */
public record NewDocumentData(
    @NotBlank(message = "Name cannot be empty")
        @Size(max = 50, message = "Document name cannot exceed 50 characters")
        @Pattern(
            regexp = "^[a-zA-Z0-9]*$",
            message =
                "Document name contains invalid characters. Only letters and numbers are allowed.")
        String name,
    Set<
            @NotBlank(message = "Tag cannot be empty")
            @Size(max = 20, message = "Tag name cannot exceed 20 characters") String>
        tags) {}
