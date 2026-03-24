package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.entity.DocumentMetadata;
import com.clara.ops.challenge.document_management_service_challenge.entity.User;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentMetadataRepository;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides services to manage document metadata, including saving, querying, and retrieving
 * metadata related to documents owned by users.
 */
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

  /**
   * Finds documents based on the provided search criteria, such as their name, tags, and owner. If
   * the name and tags are not provided, the documents are listed based on the owner. The search
   * results are paginated.
   *
   * @param name The partial or full name of the document to find. If null or blank, this criterion
   *     is ignored.
   * @param tags A set of tags associated with the documents to find. If null or empty, this
   *     criterion is ignored.
   * @param owner The user who owns the documents being searched. This criterion is mandatory.
   * @param pageable A pageable parameter defining the pagination and sorting of the results.
   * @return A paginated {@code Page<DocumentMetadata>} containing the documents matching the
   *     provided search criteria.
   */
  public Page<DocumentMetadata> findDocuments(
      String name, Set<String> tags, User owner, Pageable pageable) {
    var checkName = name != null && !name.trim().isBlank();
    var checkTags = tags != null && !tags.isEmpty();
    if (checkName && checkTags) {
      return documentMetadataRepository.findByNameLikeAndTagsInAndOwner(
          "%%%s%%".formatted(name), tags, owner, pageable);
    } else if (checkName) {
      return documentMetadataRepository.findByNameLikeAndOwner(
          "%%%s%%".formatted(name), owner, pageable);
    } else if (checkTags) {
      return documentMetadataRepository.findByTagsInAndOwner(tags, owner, pageable);
    } else {
      return documentMetadataRepository.findByOwner(owner, pageable);
    }
  }
}
