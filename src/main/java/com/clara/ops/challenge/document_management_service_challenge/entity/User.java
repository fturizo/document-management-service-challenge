package com.clara.ops.challenge.document_management_service_challenge.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "document_users", schema = "document_schema")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class User implements UserDetails, Serializable {

  @Id
  @Column(name = "id", columnDefinition = "INT")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Integer id;

  @Column(
      name = "username",
      columnDefinition = "VARCHAR",
      length = 25,
      nullable = false,
      unique = true)
  private String username;

  @Column(name = "password", columnDefinition = "VARCHAR", length = 150, nullable = false)
  @ToString.Exclude
  private String password;

  @Column(name = "full_name", columnDefinition = "VARCHAR", length = 100, nullable = false)
  private String fullName;

  public User(String username, String password, String fullName) {
    this.username = username;
    this.password = password;
    this.fullName = fullName;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return new ArrayList<>();
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
    User user = (User) o;
    return getId() != null && Objects.equals(getId(), user.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
