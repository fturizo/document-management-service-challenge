package com.clara.ops.challenge.document_management_service_challenge.exceptions;

/**
 * Exception thrown when validation errors occur during document operations. <br>
 * This runtime exception is used to indicate issues related to the validation of documents, such as
 * invalid or malformed data provided for a document operation. <br>
 * Extends {@link RuntimeException} to represent an unchecked exception.
 */
public class DocumentValidationException extends RuntimeException {
  public DocumentValidationException(String message) {
    super(message);
  }
}
