package com.clara.ops.challenge.document_management_service_challenge.repository;

import com.clara.ops.challenge.document_management_service_challenge.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing and querying {@link User} entities. <br>
 * This interface provides methods for performing standard CRUD operations as well as custom query
 * operations related to {@link User} entities. <br>
 * Extends {@link JpaRepository} to inherit JPA repository functionalities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

  /**
   * Finds a {@link User} entity based on the given username.
   *
   * @param username the username of the user to find; must not be null
   * @return an {@link Optional} containing the found {@link User} if it exists, or an empty {@link
   *     Optional} if no user is found
   */
  Optional<User> findByUsername(String username);
}
