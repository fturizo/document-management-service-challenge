package com.clara.ops.challenge.document_management_service_challenge.dto;

import java.util.Set;

/**
 * Used to hold information on the document data provided during an upload operation.
 *
 * @param name The name of the document
 * @param tags A set of tags attached to the document
 */
public record DocumentData(String name, Set<String> tags) {}
