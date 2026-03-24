package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.entity.User;
import com.clara.ops.challenge.document_management_service_challenge.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * A service for managing user entity data. Provides functionalities to save new users and retrieve
 * user profiles via ID. <br>
 * This class also implements the {@link UserDetailsService} interface to function as a Spring
 * Security authentication service.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Saves a new user in the system.
   *
   * @param username The username, which must be unique.
   * @param password The user's password in plain text format, it will be encoded internally.
   * @param fullName The full name of the user.
   * @return A new {@link User} entity persisted in the system's database.
   */
  public User saveNewUser(String username, String password, String fullName) {
    var user = new User(username, passwordEncoder.encode(password), fullName);
    userRepository.save(user);
    return user;
  }

  /**
   * Retrieves the data of an existing user.
   *
   * @param username The username of the user entity in question.
   * @return An {@link Optional} instance which contains the user entity that matches the username,
   *     empty otherwise.
   */
  public Optional<User> getUser(String username) {
    return userRepository.findByUsername(username);
  }

  /**
   * Behaves exactly as the {@link #getUser} method, used by Spring Security to load authenticated
   * profile data.
   *
   * @param username The username to authenticate.
   * @return An {@link User} instance that matches the supplier username.
   * @throws UsernameNotFoundException If the username does not match an existing user entity
   *     profile.
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return getUser(username)
        .orElseThrow(() -> new UsernameNotFoundException("User %s not found".formatted(username)));
  }
}
