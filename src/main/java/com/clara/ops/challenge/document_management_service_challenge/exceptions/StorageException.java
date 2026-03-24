package com.clara.ops.challenge.document_management_service_challenge.exceptions;

/**
 * Exception thrown when an internal server error occurs during file storage operations. <br>
 * This runtime exception includes an Incident ID in its error message to assist in identifying the
 * associated log entry for further investigation and debugging. Used in scenarios where file
 * storage processes encounter unexpected errors. <br>
 * Extends {@link RuntimeException} to represent an unchecked exception.
 */
public class StorageException extends RuntimeException {

  public StorageException(String incidentID) {
    super(
        "Internal server error encountered during file storage, check log entry with Incident ID: %s"
            .formatted(incidentID));
  }
}
