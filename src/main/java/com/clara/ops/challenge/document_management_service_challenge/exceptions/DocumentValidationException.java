package com.clara.ops.challenge.document_management_service_challenge.exceptions;

/**
 * Runtime exception used to track exceptions encountered while validating document upload input
 * data.
 */
public class DocumentValidationException extends RuntimeException {
  public DocumentValidationException(String message) {
    super(message);
  }
}
