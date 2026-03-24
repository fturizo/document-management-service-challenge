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

@RestController
@RequestMapping("/document-management/")
@RequiredArgsConstructor
@Slf4j
public class DocumentManagementController {

  private final DocumentService documentService;

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

  @GetMapping(value = "/download/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DocumentLocation> getDownloadData(@PathVariable long id) {
    var location = documentService.retrieveLocation(id, currentUser());
    return ResponseEntity.ok(location);
  }

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
