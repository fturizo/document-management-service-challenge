package com.clara.ops.challenge.document_management_service_challenge.controller.dto;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Represents metadata about a document in the system.
 *
 * @param id The unique identifier of the document.
 * @param name The name of the document.
 * @param fileSize The size of the document in bytes.
 * @param createdAt The date and time the document was created.
 * @param tags A set of tags associated with the document.
 */
public record DocumentData(
    long id, String name, long fileSize, LocalDateTime createdAt, Set<String> tags) {}
