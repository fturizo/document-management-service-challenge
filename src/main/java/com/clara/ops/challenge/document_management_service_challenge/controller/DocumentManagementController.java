package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/document-management/")
public class DocumentManagementController {

  @PostMapping("/upload")
  public ResponseEntity<Void> uploadNewDocument(
      @RequestPart("file") MultipartFile document, @RequestPart DocumentData data) {
    return ResponseEntity.ok().build();
  }
}
