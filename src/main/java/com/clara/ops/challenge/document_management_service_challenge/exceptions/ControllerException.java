package com.clara.ops.challenge.document_management_service_challenge.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Generic exception used to report REST controller errors */
public class ControllerException extends RuntimeException {

  @Getter private final HttpStatus status;

  public ControllerException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }
}
