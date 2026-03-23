package com.clara.ops.challenge.document_management_service_challenge.exceptions;

/**
 * Exception thrown when a document's metadata with the specified ID cannot be found.
 *
 * <p>This runtime exception indicates that the requested document does not exist in the system.
 * <br>
 * Extends {@link RuntimeException} to represent an unchecked exception.
 */
public class DocumentNotFoundException extends RuntimeException {
  public DocumentNotFoundException(long id) {
    super("The document with ID %d does not exists".formatted(id));
  }
}
