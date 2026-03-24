package com.clara.ops.challenge.document_management_service_challenge.exceptions;

import lombok.RequiredArgsConstructor;

/**
 * Exception thrown when attempting to create or upload a document that already exists in the
 * system. <br>
 * This runtime exception is typically used in scenarios where a document with the specified name
 * conflicts with an existing document. It provides a message detailing the name of the conflicting
 * document. <br>
 * Extends {@link RuntimeException} to represent an unchecked exception.
 */
@RequiredArgsConstructor
public class DocumentConflictException extends RuntimeException {

  public DocumentConflictException(String documentName) {
    super("Document with name %s already exists".formatted(documentName));
  }
}
