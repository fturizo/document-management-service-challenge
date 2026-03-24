package com.clara.ops.challenge.document_management_service_challenge.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception used to indicate controller-level errors in the application. <br>
 * This exception extends {@link RuntimeException} and provides additional information about HTTP
 * status codes to better communicate the nature of the error encountered during request processing.
 * <br>
 * Typically thrown in scenarios where an unexpected condition occurs in the application's REST
 * controllers, and an appropriate HTTP response needs to be generated with both an error message
 * and an associated HTTP status.
 */
public class ControllerException extends RuntimeException {

  @Getter private final HttpStatus status;

  public ControllerException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }
}
