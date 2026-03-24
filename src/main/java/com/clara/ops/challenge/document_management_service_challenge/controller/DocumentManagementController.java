package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.controller.dto.DocumentData;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.DocumentLocation;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.NewDocumentData;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.SearchData;
import com.clara.ops.challenge.document_management_service_challenge.entity.User;
import com.clara.ops.challenge.document_management_service_challenge.exceptions.ControllerException;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Controller that handles operations related to document management such as uploading, downloading,
 * and searching for documents.
 *
 * <p>Provides REST endpoints to perform these operations while considering the user's permissions
 * and ensuring proper validation of input data.
 */
@RestController
@RequestMapping("/document-management/")
@RequiredArgsConstructor
@Slf4j
public class DocumentManagementController {

  private final DocumentService documentService;

  /**
   * Handles the uploading of a new document by validating the document metadata and file content,
   * saving the document details, and constructing the URI location of the created resource.
   *
   * @param document the file being uploaded as a {@code MultipartFile} object
   * @param data metadata about the document, including its name and tags, provided as {@code
   *     NewDocumentData}
   * @param ucb utility to build the URI location of the uploaded document
   * @return a {@code ResponseEntity<Void>} with an HTTP status of 201 (Created) upon successful
   *     upload, including the location of the new document in the response header
   * @throws ControllerException if the content type is invalid or an I/O error occurs during
   *     processing
   */
  @PostMapping("/upload")
  public ResponseEntity<Void> uploadNewDocument(
      @RequestPart("file") MultipartFile document,
      @RequestPart @Valid NewDocumentData data,
      UriComponentsBuilder ucb)
      throws ControllerException {
    var contentType = document.getContentType();
    var currentUser = currentUser();
    log.debug("Received request to upload new document by user {} ", currentUser);
    if (contentType == null) {
      throw new ControllerException(
          HttpStatus.BAD_REQUEST, "Content type of uploaded file part cannot be determined");
    }
    try {
      var metadata =
          documentService.saveDocument(
              data, document.getInputStream(), contentType, document.getSize(), currentUser);
      var location =
          ucb.path("document-management/download/{id}").buildAndExpand(metadata.getId()).toUri();
      return ResponseEntity.created(location).build();
    } catch (IOException exception) {
      throw new ControllerException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "IO error encountered during file upload: %s".formatted(exception.getMessage()));
    }
  }

  /**
   * Retrieves the download location of a specific document identified by its ID. <br>
   * This method validates the requesting user's access rights and ensures the document exists.
   *
   * @param id The unique identifier of the document whose download location is being requested.
   * @return A {@code ResponseEntity<DocumentLocation>} containing the temporary location details
   *     such as the download URL and expiration information.
   */
  @GetMapping(value = "/download/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DocumentLocation> getDownloadData(@PathVariable long id) {
    var location = documentService.retrieveLocation(id, currentUser());
    return ResponseEntity.ok(location);
  }

  /**
   * Searches for documents in the system based on the provided search criteria and pagination
   * options.
   *
   * @param pageable The pagination and sorting information, including page size, sort field, and
   *     sort direction. Defaults to a page size of 20, sorted by "createdAt" in ascending order.
   * @param data The search criteria, encapsulated in a {@code SearchData} object, including the
   *     document name and tags to filter by.
   * @return A {@code PagedModel<DocumentData>} containing the paginated results of documents that
   *     match the search criteria.
   */
  @GetMapping(
      value = "/search",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public PagedModel<DocumentData> searchDocuments(
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
          Pageable pageable,
      @RequestBody SearchData data) {
    var requester = currentUser();
    var page = documentService.searchDocuments(data.name(), data.tags(), requester, pageable);
    return new PagedModel<>(page);
  }

  private User currentUser() {
    return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}
