package com.clara.ops.challenge.document_management_service_challenge.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

@Entity
@Table(name = "document_metadata", schema = "document_schema")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class DocumentMetadata implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  @Column(name = "id", columnDefinition = "BIGINT")
  private Long id;

  @Column(name = "document_name", nullable = false, columnDefinition = "VARCHAR", length = 50)
  private String name;

  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
  @Setter(AccessLevel.NONE)
  private LocalDateTime createdAt;

  @Column(name = "file_size", nullable = false, columnDefinition = "INTEGER")
  private Long fileSize;

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

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(
      name = "owner_id",
      referencedColumnName = "id",
      columnDefinition = "INT",
      nullable = false)
  @ToString.Exclude
  private User owner;

  public DocumentMetadata(String name, long fileSize, Set<String> tags, User owner) {
    this.name = name;
    this.fileSize = fileSize;
    this.tags = Collections.unmodifiableSet(tags);
    this.createdAt = LocalDateTime.now();
    this.owner = owner;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass =
        o instanceof HibernateProxy
            ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
    Class<?> thisEffectiveClass =
        this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    DocumentMetadata that = (DocumentMetadata) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
