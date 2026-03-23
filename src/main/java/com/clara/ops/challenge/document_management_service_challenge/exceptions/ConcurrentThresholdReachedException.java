package com.clara.ops.challenge.document_management_service_challenge.exceptions;

/** Runtime exception triggered when the threshold of concurrent uploads has been reached. */
public class ConcurrentThresholdReachedException extends RuntimeException {

  public ConcurrentThresholdReachedException(int uploadThreshold) {
    super(
        "Cannot upload this document as the threshold of %s concurrent uploads has been reached. Please try again later"
            .formatted(uploadThreshold));
  }
}
