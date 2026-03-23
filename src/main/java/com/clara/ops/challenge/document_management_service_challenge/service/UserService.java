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

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public User saveNewUser(String username, String password, String fullName) {
    var user = new User(username, passwordEncoder.encode(password), fullName);
    userRepository.save(user);
    return user;
  }

  public Optional<User> getUser(String username) {
    return userRepository.findByUsername(username);
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return getUser(username)
        .orElseThrow(() -> new UsernameNotFoundException("User %s not found".formatted(username)));
  }
}
