package com.clara.ops.challenge.document_management_service_challenge.exceptions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocumentConflictException extends RuntimeException {

  private final String documentName;

  public String getMessage() {
    return String.format("Document with name %s already exists", documentName);
  }
}
