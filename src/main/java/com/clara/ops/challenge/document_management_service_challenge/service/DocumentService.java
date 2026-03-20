package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentMetadataRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentService {

  private final DocumentMetadataRepository documentMetadataRepository;

  @Transactional
  public boolean uploadDocument(String name, Set<String> tags) {
    return false;
  }
}
