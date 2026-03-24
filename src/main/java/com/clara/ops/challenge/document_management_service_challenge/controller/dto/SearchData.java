package com.clara.ops.challenge.document_management_service_challenge.controller.dto;

import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * Encapsulates the search criteria for filtering documents in the document management system.
 *
 * @param name The name of the document to search for. The maximum length is 50 characters.
 * @param tags A set of tags to filter the documents by. Each tag has a maximum length of 20
 *     characters.
 */
public record SearchData(
    @Size(max = 50, message = "Document search name cannot exceed 50 characters") String name,
    Set<@Size(max = 20, message = "Tag search name cannot exceed 20 characters") String> tags) {}
