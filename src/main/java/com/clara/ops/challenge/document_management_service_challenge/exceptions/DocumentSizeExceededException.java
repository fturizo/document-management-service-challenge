package com.clara.ops.challenge.document_management_service_challenge.exceptions;

/**
 * Exception thrown when the size of an uploaded document exceeds the allowed maximum limit. <br>
 * This runtime exception is typically raised during file upload operations to indicate that the
 * file size provided by the user exceeds the maximum file size constraint defined for the system.
 * The exception provides clarity on the limitation by including the maximum allowed size in the
 * error message. <br>
 * Extends {@link RuntimeException} to represent an unchecked exception.
 */
public class DocumentSizeExceededException extends RuntimeException {
  public DocumentSizeExceededException(int maxSize) {
    super("File size cannot exceed %dMB".formatted(maxSize));
  }
}
