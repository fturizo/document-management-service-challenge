package com.clara.ops.challenge.document_management_service_challenge.controller.advice;

import com.clara.ops.challenge.document_management_service_challenge.exceptions.*;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GeneralExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorDetails> handleValidationErrors(
      MethodArgumentNotValidException exception) {

    var errors =
        exception.getBindingResult().getFieldErrors().stream()
            .map(error -> "[%s] - %s".formatted(error.getField(), error.getDefaultMessage()))
            .toList();

    return ResponseEntity.badRequest()
        .body(new ErrorDetails("Validation errors encountered during processing: %s", errors));
  }

  @ExceptionHandler(ControllerException.class)
  public ResponseEntity<ErrorDetails> handleControllerErrors(ControllerException exception) {
    return ResponseEntity.status(exception.getStatus())
        .body(new ErrorDetails(exception.getMessage(), Collections.emptyList()));
  }

  @ExceptionHandler(DocumentValidationException.class)
  public ResponseEntity<ErrorDetails> handleDocumentValidationException(
      DocumentValidationException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorDetails(exception.getMessage(), Collections.emptyList()));
  }

  @ExceptionHandler(DocumentConflictException.class)
  public ResponseEntity<ErrorDetails> handleDocumentConflictException(
      DocumentConflictException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorDetails(exception.getMessage(), Collections.emptyList()));
  }

  @ExceptionHandler(ConcurrentThresholdReachedException.class)
  public ResponseEntity<ErrorDetails> handleConcurrentThresholdReachedException(
      ConcurrentThresholdReachedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(new ErrorDetails(exception.getMessage(), Collections.emptyList()));
  }

  @ExceptionHandler(StorageException.class)
  public ResponseEntity<ErrorDetails> handleStorageException(StorageException exception) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorDetails(exception.getMessage(), Collections.emptyList()));
  }

  public record ErrorDetails(String message, List<String> errors) {}
}
