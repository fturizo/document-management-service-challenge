package com.clara.ops.challenge.document_management_service_challenge.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "document_metadata", schema = "document_schema")
@Data
@NoArgsConstructor
public class DocumentMetadata implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", columnDefinition = "BIGINT")
  private Long id;

  @Column(name = "document_name", nullable = false, columnDefinition = "VARCHAR", length = 50)
  private String name;

  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime createdAt;

  @Column(name = "file_size", nullable = false, columnDefinition = "INTEGER")
  private Integer fileSize;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "document_tags",
      joinColumns =
          @JoinColumn(
              name = "document_id",
              referencedColumnName = "id",
              columnDefinition = "BIGINT",
              nullable = false),
      schema = "document_schema")
  @Column(name = "tag", nullable = false, columnDefinition = "VARCHAR", length = 20)
  private Set<String> tags;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "owner_id",
      referencedColumnName = "id",
      columnDefinition = "INT",
      nullable = false)
  private User owner;

  public DocumentMetadata(String name, int fileSize, Set<String> tags, User owner) {
    this.name = name;
    this.fileSize = fileSize;
    this.tags = Collections.unmodifiableSet(tags);
    this.createdAt = LocalDateTime.now();
    this.owner = owner;
  }
}
