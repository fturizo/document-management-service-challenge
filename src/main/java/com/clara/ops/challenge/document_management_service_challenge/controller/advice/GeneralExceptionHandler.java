package com.clara.ops.challenge.document_management_service_challenge.controller.advice;

import com.clara.ops.challenge.document_management_service_challenge.exceptions.*;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handler for the application that manages various types of exceptions and
 * formats error responses consistently. This class leverages Spring's {@code @RestControllerAdvice}
 * to intercept and process exceptions globally. <br>
 * The handled exceptions include: - {@link MethodArgumentNotValidException}: Thrown when a method
 * argument fails validation. - {@link ControllerException}: Represents generic REST controller
 * exceptions. - {@link DocumentValidationException}: Indicates a validation error related to
 * document input. - {@link DocumentConflictException}: Specifies conflicts when a document with the
 * same name exists. - {@link DocumentNotFoundException}: Specifies that a document cannot be found
 * in the system. - {@link DocumentAccessException}: Indicates forbidden access to a document. -
 * {@link ConcurrentThresholdReachedException}: Triggered when the concurrent upload limit is
 * exceeded. - {@link StorageException}: Handles internal server errors related to storage
 * operations. <br>
 * Custom error responses are encapsulated in the {@code ErrorDetails} DTO record, which includes a
 * general message and a list of specific errors (if applicable).
 */
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
        .body(new ErrorDetails(exception.getMessage()));
  }

  @ExceptionHandler(DocumentValidationException.class)
  public ResponseEntity<ErrorDetails> handleDocumentValidationException(
      DocumentValidationException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorDetails(exception.getMessage()));
  }

  @ExceptionHandler(DocumentSizeExceededException.class)
  public ResponseEntity<ErrorDetails> handleDocumentSizeExceededException(
      DocumentSizeExceededException exception) {
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
        .body(new ErrorDetails(exception.getMessage()));
  }

  @ExceptionHandler(DocumentConflictException.class)
  public ResponseEntity<ErrorDetails> handleDocumentConflictException(
      DocumentConflictException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorDetails(exception.getMessage()));
  }

  @ExceptionHandler(ConcurrentThresholdReachedException.class)
  public ResponseEntity<ErrorDetails> handleConcurrentThresholdReachedException(
      ConcurrentThresholdReachedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(new ErrorDetails(exception.getMessage()));
  }

  @ExceptionHandler(StorageException.class)
  public ResponseEntity<ErrorDetails> handleStorageException(StorageException exception) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorDetails(exception.getMessage()));
  }

  @ExceptionHandler(InvalidSortCriteriaException.class)
  public ResponseEntity<ErrorDetails> handleInvalidSortCriteriaException(
      InvalidSortCriteriaException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorDetails(exception.getMessage()));
  }

  @ExceptionHandler(DocumentNotFoundException.class)
  public ResponseEntity<ErrorDetails> handleDocumentNotFoundException(
      DocumentNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorDetails(exception.getMessage()));
  }

  @ExceptionHandler(DocumentAccessException.class)
  public ResponseEntity<ErrorDetails> handleDocumentAccessException(
      DocumentAccessException exception) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new ErrorDetails(exception.getMessage()));
  }

  public record ErrorDetails(String message, List<String> errors) {
    public ErrorDetails(String message) {
      this(message, Collections.emptyList());
    }
  }
}
