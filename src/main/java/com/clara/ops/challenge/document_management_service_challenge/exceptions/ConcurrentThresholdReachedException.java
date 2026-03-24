package com.clara.ops.challenge.document_management_service_challenge.exceptions;

/**
 * Exception thrown when the maximum threshold for concurrent uploads has been reached.
 *
 * <p>This runtime exception is typically raised during document upload operations to signal that
 * the number of concurrent uploads currently in progress has exceeded the allowed limit. The
 * exception message includes the configured threshold value, providing clarity on the imposed
 * restriction. <br>
 * Extends {@link RuntimeException} to represent an unchecked exception.
 */
public class ConcurrentThresholdReachedException extends RuntimeException {

  public ConcurrentThresholdReachedException(int uploadThreshold) {
    super(
        "Cannot upload this document as the threshold of %s concurrent uploads has been reached. Please try again later"
            .formatted(uploadThreshold));
  }
}
