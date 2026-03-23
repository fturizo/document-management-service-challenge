package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.entity.DocumentMetadata;
import com.clara.ops.challenge.document_management_service_challenge.entity.User;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentMetadataRepository;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MetadataService {

  private final DocumentMetadataRepository documentMetadataRepository;

  /**
   * Persists metadata for a document and returns the saved metadata entity.
   *
   * @param name The name of the document to which the metadata belongs.
   * @param fileSize The size of the document file in bytes.
   * @param tags A set of tags associated with the document.
   * @param owner The user who owns the document.
   * @return The saved {@code DocumentMetadata} entity containing the given properties.
   */
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

  /**
   * Retrieves the metadata for a document based on its identifier.
   *
   * @param id The unique identifier of the document whose metadata is being retrieved.
   * @return An {@code Optional} containing the {@code DocumentMetadata} if found, or an empty
   *     {@code Optional} if no metadata exists for the given ID.
   */
  public Optional<DocumentMetadata> retrieveMetadata(long id) {
    return documentMetadataRepository.findById(id);
  }
}
