package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.controller.dto.UserData;
import com.clara.ops.challenge.document_management_service_challenge.exceptions.ControllerException;
import com.clara.ops.challenge.document_management_service_challenge.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A simple REST controller that allows users to registers themselves in the service to upload and
 * interact with their documents.
 */
@RestController
@RequestMapping("/registration")
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {

  private final UserService userService;

  /**
   * Registers a new user in the service.
   *
   * @param data Contains the new user information to register
   * @return A response with the outcome of the operation
   */
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> saveNewUser(@RequestBody @Valid UserData data) {
    if (userService.getUser(data.username()).isPresent()) {
      throw new ControllerException(HttpStatus.CONFLICT, "Username already exists");
    }
    log.debug("Registering new user data: {} - {}", data.username(), data.fullName());
    var newUser = userService.saveNewUser(data.username(), data.password(), data.fullName());
    log.debug(
        "New user data registered successfully for user: {} using ID: {}",
        newUser.getUsername(),
        newUser.getId());
    return ResponseEntity.ok().build();
  }
}
