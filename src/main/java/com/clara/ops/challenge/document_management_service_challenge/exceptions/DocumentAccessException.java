package com.clara.ops.challenge.document_management_service_challenge.exceptions;

/**
 * Exception thrown when a user attempts to access a document they do not own. <br>
 * Extends {@link RuntimeException} to represent an unchecked exception.
 */
public class DocumentAccessException extends RuntimeException {

  public DocumentAccessException() {
    super("The user requesting access to this document is not its owner");
  }
}
