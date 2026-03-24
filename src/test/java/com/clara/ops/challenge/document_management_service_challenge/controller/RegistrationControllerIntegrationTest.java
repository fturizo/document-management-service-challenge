package com.clara.ops.challenge.document_management_service_challenge.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.clara.ops.challenge.document_management_service_challenge.BaseIntegrationTest;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.UserData;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RegistrationControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  @Order(1)
  @DisplayName("Successful user registration")
  void registerNewUser_ShouldSucceed() {
    UserData userData = new UserData("testuser123", "password123", "Test User Full Name");
    ResponseEntity<Void> response =
        restTemplate.postForEntity("/registration", userData, Void.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  @Order(2)
  @DisplayName("User registration fails on duplicate")
  void registerDuplicateUser_ShouldFail() {
    UserData userData = new UserData("duplicateuser", "password123", "Duplicate User Full Name");
    restTemplate.postForEntity("/registration", userData, Void.class);

    ResponseEntity<Void> secondResponse =
        restTemplate.postForEntity("/registration", userData, Void.class);

    assertEquals(HttpStatus.CONFLICT, secondResponse.getStatusCode());
  }

  @Test
  @Order(1)
  @DisplayName("Invalid user registration")
  void registerInvalidUser_ShouldReturnBadRequest() {
    UserData invalidUser = new UserData("usr", "short", "Short"); // Too short
    ResponseEntity<Void> response =
        restTemplate.postForEntity("/registration", invalidUser, Void.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }
}
