package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.controller.dto.DocumentData;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.DocumentLocation;
import com.clara.ops.challenge.document_management_service_challenge.entity.DocumentMetadata;
import com.clara.ops.challenge.document_management_service_challenge.entity.User;
import com.clara.ops.challenge.document_management_service_challenge.exceptions.*;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

  public DocumentMetadata saveDocument(
      DocumentData data, InputStream contentStream, String contentType, long fileSize, User owner) {
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
