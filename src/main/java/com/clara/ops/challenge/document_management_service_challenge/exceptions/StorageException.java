package com.clara.ops.challenge.document_management_service_challenge.exceptions;

/**
 * Runtime exception used to inform of unexpected errors encountered during storage service
 * operations.
 */
public class StorageException extends RuntimeException {

  /**
   * Creates a new storage exception.
   *
   * @param incidentID An internal incident reference to track the error on the application logs.
   */
  public StorageException(String incidentID) {
    super(
        "Internal server error encountered during file storage, check log entry with Incident ID: %s"
            .formatted(incidentID));
  }
}
