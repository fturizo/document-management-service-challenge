package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.entity.DocumentMetadata;
import com.clara.ops.challenge.document_management_service_challenge.entity.User;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentMetadataRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MetadataService {

  private final DocumentMetadataRepository documentMetadataRepository;

  @Transactional
  public DocumentMetadata saveMetadata(String name, long fileSize, Set<String> tags, User owner) {
    var metadata = new DocumentMetadata(name, fileSize, tags, owner);
    documentMetadataRepository.save(metadata);
    return metadata;
  }

  /**
   * Checks if there is existing metadata for a document with the supplied name owned by the
   * matching user.
   *
   * @param name The name of the document
   * @param owner The user that may own the document matching the name in question.
   * @return <code>true</code> if a document with the name matches the same owner, <code>false
   *     </code> otherwise
   */
  public boolean documentExists(String name, User owner) {
    return documentMetadataRepository.findByNameAndOwner(name, owner).stream()
        .findFirst()
        .isPresent();
  }
}
