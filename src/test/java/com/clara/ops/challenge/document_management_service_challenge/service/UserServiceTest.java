package com.clara.ops.challenge.document_management_service_challenge.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.entity.User;
import com.clara.ops.challenge.document_management_service_challenge.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserService userService;

  @Test
  void saveNewUser_ShouldEncryptPasswordAndSaveUser() {
    String username = "testuser";
    String password = "password";
    String fullName = "Test User";
    String encryptedPassword = "encryptedPassword";

    when(passwordEncoder.encode(password)).thenReturn(encryptedPassword);

    User savedUser = userService.saveNewUser(username, password, fullName);

    assertNotNull(savedUser);
    assertEquals(username, savedUser.getUsername());
    assertEquals(encryptedPassword, savedUser.getPassword());
    assertEquals(fullName, savedUser.getFullName());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void getUser_ShouldReturnUser_WhenExists() {
    String username = "testuser";
    User user = new User(username, "password", "Test User");
    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

    Optional<User> result = userService.getUser(username);

    assertTrue(result.isPresent());
    assertEquals(username, result.get().getUsername());
  }

  @Test
  void getUser_ShouldReturnEmpty_WhenDoesNotExist() {
    String username = "nonexistent";
    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

    Optional<User> result = userService.getUser(username);

    assertTrue(result.isEmpty());
  }

  @Test
  void loadUserByUsername_ShouldReturnUserDetails_WhenExists() {
    String username = "testuser";
    User user = new User(username, "password", "Test User");
    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

    UserDetails userDetails = userService.loadUserByUsername(username);

    assertNotNull(userDetails);
    assertEquals(username, userDetails.getUsername());
  }

  @Test
  void loadUserByUsername_ShouldThrowException_WhenDoesNotExist() {
    String username = "nonexistent";
    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

    assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(username));
  }
}
