package com.clara.ops.challenge.document_management_service_challenge.repository;

import com.clara.ops.challenge.document_management_service_challenge.entity.DocumentMetadata;
import com.clara.ops.challenge.document_management_service_challenge.entity.User;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing and querying {@link DocumentMetadata} entities. <br>
 * This interface provides methods for performing CRUD operations as well as custom query operations
 * based on specific attributes of the {@link DocumentMetadata} entity. <br>
 * Extends {@link JpaRepository} to inherit standard JPA functionalities.
 */
@Repository
public interface DocumentMetadataRepository extends JpaRepository<DocumentMetadata, Long> {

  /**
   * Finds a list of {@link DocumentMetadata} entities by their name and owner.
   *
   * @param name the name of the document to search for
   * @param owner the owner of the document
   * @return a list of {@link DocumentMetadata} entities that match the specified name and owner
   */
  List<DocumentMetadata> findByNameAndOwner(String name, User owner);

  /**
   * Retrieves a paginated list of {@link DocumentMetadata} entities for a given owner.
   *
   * @param owner the owner of the documents to retrieve
   * @param pageable the pagination and sorting information
   * @return a paginated list of {@link DocumentMetadata} entities that belong to the specified
   *     owner
   */
  Page<DocumentMetadata> findByOwner(User owner, Pageable pageable);

  /**
   * Retrieves a paginated list of {@link DocumentMetadata} entities where the name matches the
   * specified pattern and the owner is the specified user.
   *
   * @param name the pattern to match the document name against (using a SQL <code>LIKE</code>
   *     comparison)
   * @param owner the owner of the documents to retrieve
   * @param pageable the pagination and sorting information
   * @return a paginated list of {@link DocumentMetadata} entities that match the specified name
   *     pattern and owner
   */
  Page<DocumentMetadata> findByNameLikeAndOwner(String name, User owner, Pageable pageable);

  /**
   * Retrieves a paginated list of {@link DocumentMetadata} entities that are associated with a
   * given owner and contain any of the specified tags.
   *
   * @param tags the set of tags to search for in the documents
   * @param owner the owner of the documents to retrieve
   * @param pageable pagination and sorting information
   * @return a paginated list of {@link DocumentMetadata} entities that match the specified tags and
   *     owner
   */
  Page<DocumentMetadata> findByTagsInAndOwner(Set<String> tags, User owner, Pageable pageable);

  /**
   * Retrieves a paginated list of {@link DocumentMetadata} entities where the name matches the
   * specified pattern, the tags intersect with the provided set of tags, and the owner is the
   * specified user.
   *
   * @param name the pattern to match the document name against (using a SQL LIKE comparison)
   * @param tags the set of tags to search for in the documents
   * @param owner the owner of the documents to retrieve
   * @param pageable pagination and sorting information
   * @return a paginated list of {@link DocumentMetadata} entities that match the specified name
   *     pattern, tags, and owner
   */
  Page<DocumentMetadata> findByNameLikeAndTagsInAndOwner(
      String name, Set<String> tags, User owner, Pageable pageable);
}
