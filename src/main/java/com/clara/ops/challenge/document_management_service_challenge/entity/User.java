package com.clara.ops.challenge.document_management_service_challenge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "document_users", schema = "document_schema")
@Data
@NoArgsConstructor
public class User implements UserDetails, Serializable {

  @Id
  @Column(name = "id", columnDefinition = "INT")
  private Integer id;

  @Column(name = "username", columnDefinition = "VARCHAR", length = 25)
  private String username;

  @Column(name = "password", columnDefinition = "VARCHAR", length = 150)
  private String password;

  @Column(name = "full_name", columnDefinition = "VARCHAR", length = 100)
  private String fullName;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return new ArrayList<>();
  }
}
