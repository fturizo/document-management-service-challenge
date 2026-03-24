package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.controller.dto.DocumentData;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.DocumentLocation;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.NewDocumentData;
import com.clara.ops.challenge.document_management_service_challenge.entity.DocumentMetadata;
import com.clara.ops.challenge.document_management_service_challenge.entity.User;
import com.clara.ops.challenge.document_management_service_challenge.exceptions.*;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.stereotype.Service;

/**
 * Service class entrypoint for managing documents, including saving, retrieving locations, and
 * searching. This class provides functionality to handle document storage, metadata management, and
 * ensures proper validation and error handling. <br>
 * <b>IMPORTANT:</b> All operations that involve the manipulation and data of documents should go
 * through this class.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

  private static final int MAX_NUMBER_CONCURRENT_UPLOADS = 10;
  private static final int MAX_FILE_SIZE_MB = 500;
  private static final int BYTES_MB_EQUIVALENT = 1024 * 1024;

  private final StorageService storageService;
  private final MetadataService metadataService;
  private final AtomicInteger concurrentUploads = new AtomicInteger(0);

  /**
   * Saves a new document along with its metadata. Performs validations on file type, size, and
   * handles concurrency limits for uploads. Upon successful storage, metadata is saved.
   *
   * @param data The metadata details for the document being saved.
   * @param contentStream The input stream of the document content.
   * @param contentType The MIME type of the document (e.g., application/pdf).
   * @param fileSize The size of the document in bytes.
   * @param owner The owner of the document.
   * @return The metadata of the saved document.
   * @throws ConcurrentThresholdReachedException If the maximum number of concurrent uploads is
   *     reached.
   * @throws DocumentValidationException If the document type is invalid or the file size exceeds
   *     the limit.
   * @throws DocumentConflictException If a document with the same name already exists for the
   *     owner.
   * @throws StorageException If the document storage process fails.
   */
  public DocumentMetadata saveDocument(
      NewDocumentData data,
      InputStream contentStream,
      String contentType,
      long fileSize,
      User owner) {
    if (concurrentUploads.get() == MAX_NUMBER_CONCURRENT_UPLOADS) {
      throw new ConcurrentThresholdReachedException(MAX_NUMBER_CONCURRENT_UPLOADS);
    }
    try {
      concurrentUploads.incrementAndGet();
      if (!contentType.equalsIgnoreCase("application/pdf")) {
        throw new DocumentValidationException(
            "Invalid type of document detected, only PDF documents are accepted");
      } else if (metadataService.documentExists(data.name(), owner)) {
        throw new DocumentConflictException(data.name());
      } else if (fileSize / BYTES_MB_EQUIVALENT > MAX_FILE_SIZE_MB) {
        throw new DocumentValidationException(
            "File size cannot exceed %dMB".formatted(MAX_FILE_SIZE_MB));
      }
      var storageOutcome =
          storageService.storeFile(
              owner.getUsername(), data.name(), fileSize, contentType, contentStream);
      if (!storageOutcome.success()) {
        processErrorOutcome(storageOutcome);
      }
      // TODO - Validate that transaction completes correctly and if not, retry the operation and if
      // possible, delete the file if saving the metadata is not possible
      return metadataService.saveMetadata(data.name(), fileSize, data.tags(), owner);
    } finally {
      concurrentUploads.decrementAndGet();
    }
  }

  /**
   * Retrieves the location of a document for downloading. This method ensures that the requesting
   * user has the necessary access to the document and validates the existence of the document
   * before generating the location details.
   *
   * @param id The unique identifier of the document whose location is to be retrieved.
   * @param requester The user requesting access to the document's location.
   * @return The temporary location of the document, including the URL and expiration details.
   * @throws DocumentNotFoundException If the document with the specified ID is not found.
   * @throws DocumentAccessException If the requester does not have access to the document.
   * @throws StorageException If there is an issue retrieving the document's location.
   */
  public DocumentLocation retrieveLocation(long id, User requester) {
    var metadata =
        metadataService.retrieveMetadata(id).orElseThrow(() -> new DocumentNotFoundException(id));
    if (!metadata.getOwner().equals(requester)) {
      throw new DocumentAccessException();
    }
    var locationOutcome =
        storageService.getDownloadURL(metadata.getOwner().getUsername(), metadata.getName());
    if (!locationOutcome.success()) {
      processErrorOutcome(locationOutcome);
    }
    return new DocumentLocation(
        locationOutcome.message(), storageService.getExpiryTime(), storageService.getExpiryUnit());
  }

  /**
   * Searches for documents based on the given criteria. This method queries the metadata storage to
   * find documents matching the specified name, tags, or owner, and returns a paginated result.
   *
   * @param name The name of the document to search for. Allows partial or exact matches.
   * @param tags A set of tags to filter the documents by. Only documents containing matching tags
   *     will be returned.
   * @param owner The owner of the documents to filter by. Only documents belonging to this owner
   *     will be included.
   * @param pageable The pagination information specifying the page number, size, and sorting
   *     options.
   * @return A paginated list of {@link DocumentData} objects that match the search criteria.
   */
  public Page<DocumentData> searchDocuments(
      String name, Set<String> tags, User owner, Pageable pageable) {
    try {
      return metadataService.findDocuments(name, tags, owner, pageable).map(DocumentService::mapTo);
    } catch (PropertyReferenceException exception) {
      log.trace(
          "Property reference exception in document search operation: {}", exception.getMessage());
      throw new InvalidSortCriteriaException("Document", exception.getPropertyName());
    }
  }

  private static DocumentData mapTo(DocumentMetadata data) {
    return new DocumentData(
        data.getId(), data.getName(), data.getFileSize(), data.getCreatedAt(), data.getTags());
  }

  private void processErrorOutcome(StorageService.Outcome outcome) {
    var incidentID = UUID.randomUUID().toString();
    if (outcome.error() != null) {
      log.error("(IncidentID: {}) {}", incidentID, outcome.message(), outcome.error());
    } else {
      log.error("(IncidentID: {}) {}", incidentID, outcome.message());
    }
    throw new StorageException(incidentID);
  }
}
