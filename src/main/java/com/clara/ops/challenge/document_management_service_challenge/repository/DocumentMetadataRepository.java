package com.clara.ops.challenge.document_management_service_challenge.repository;

import com.clara.ops.challenge.document_management_service_challenge.entity.DocumentMetadata;
import com.clara.ops.challenge.document_management_service_challenge.entity.User;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentMetadataRepository extends JpaRepository<DocumentMetadata, Long> {

  List<DocumentMetadata> findByNameAndOwner(String name, User owner);

  Page<DocumentMetadata> findByOwner(User owner, Pageable pageable);

  Page<DocumentMetadata> findByNameLikeAndOwner(String name, User owner, Pageable pageable);

  Page<DocumentMetadata> findByTagsInAndOwner(Set<String> tags, User owner, Pageable pageable);

  Page<DocumentMetadata> findByNameLikeAndTagsInAndOwner(
      String name, Set<String> tags, User owner, Pageable pageable);
}
