package com.clara.ops.challenge.document_management_service_challenge.exceptions;

/**
 * Exception thrown when an invalid property is provided for a specified resource as a sort criteria
 * during a search operation. <br>
 * This exception is used to indicate that the sort criteria requested by the user on a search
 * endpoint is not valid for the resource being processed. The invalid property and resource name
 * are included in the error message for additional context. <br>
 * Extends {@link RuntimeException} to represent an unchecked exception.
 */
public class InvalidSortCriteriaException extends RuntimeException {
  public InvalidSortCriteriaException(String resourceName, String propertyName) {
    super(
        "Invalid sort property '%s' supplied for '%s' resource"
            .formatted(propertyName, resourceName));
  }
}
