package com.clara.ops.challenge.document_management_service_challenge.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.clara.ops.challenge.document_management_service_challenge.BaseIntegrationTest;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.*;
import io.minio.StatObjectArgs;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DocumentManagementControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  private final String username = "testuser";
  private final String password = "password123";

  @BeforeEach
  void setUp() {
    UserData userData = new UserData(username, password, "Test User Full Name");
    restTemplate.postForEntity("/registration", userData, Void.class);
  }

  @Test
  @Order(1)
  @DisplayName("Successful document upload")
  void uploadDocument_ShouldReturnCreated() {
    var filename = "testfile";
    var data = new NewDocumentData(filename, Collections.singleton("tag1"));

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(
        "file",
        new ByteArrayResource("dummy content".getBytes()) {
          @Override
          public String getFilename() {
            return "test.pdf";
          }
        });

    // Spring's MultipartFile might need the content type.
    // Usually it's inferred from the resource or specified in HttpEntity.
    HttpHeaders fileHeaders = new HttpHeaders();
    fileHeaders.setContentType(MediaType.APPLICATION_PDF);
    HttpEntity<ByteArrayResource> fileEntity =
        new HttpEntity<>(
            new ByteArrayResource("dummy content".getBytes()) {
              @Override
              public String getFilename() {
                return "test.pdf";
              }
            },
            fileHeaders);

    body.set("file", fileEntity);
    body.add("data", data);

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    ResponseEntity<Void> response =
        restTemplate
            .withBasicAuth(username, password)
            .postForEntity("/document-management/upload", requestEntity, Void.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getHeaders().getLocation());
  }

  @Test
  @Order(2)
  @DisplayName("Successful document download")
  void downloadDocument_ShouldReturnLocation() {

    var searchData = new SearchData("test", null);
    ResponseEntity<TestPagedModel> searchResponse =
        restTemplate
            .withBasicAuth(username, password)
            .exchange(
                "/document-management/search",
                HttpMethod.GET,
                new HttpEntity<>(searchData),
                TestPagedModel.class);

    assertNotNull(searchResponse.getBody());
    long id = searchResponse.getBody().getContent().get(0).id();

    ResponseEntity<DocumentLocation> response =
        restTemplate
            .withBasicAuth(username, password)
            .getForEntity("/document-management/download/" + id, DocumentLocation.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().url());
  }

  @Test
  @Order(3)
  @DisplayName("Document upload fails on file size limit exceeded")
  void uploadDocument_ShouldReturnPayloadTooLarge_WhenFileSizeExceedsLimit() throws IOException {
    var filename = "largefile";
    var data = new NewDocumentData(filename, Collections.singleton("tag1"));

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

    // 501 MB in bytes
    long size = 501L * 1024 * 1024;
    Path tempFile = Files.createTempFile("large-dummy", ".pdf");
    try (RandomAccessFile raf = new RandomAccessFile(tempFile.toFile(), "rw")) {
      raf.setLength(size);
    }

    try {
      FileSystemResource largeResource = new FileSystemResource(tempFile);
      HttpHeaders fileHeaders = new HttpHeaders();
      fileHeaders.setContentType(MediaType.APPLICATION_PDF);
      HttpEntity<FileSystemResource> filePart = new HttpEntity<>(largeResource, fileHeaders);

      body.add("file", filePart);
      body.add("data", data);

      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

      ResponseEntity<String> response =
          restTemplate
              .withBasicAuth(username, password)
              .postForEntity("/document-management/upload", requestEntity, String.class);

      assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().contains("File size cannot exceed 500MB"));
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  @Order(4)
  @DisplayName("Successful document search")
  void searchDocuments_ShouldReturnResults() {
    var searchData = new SearchData("test", Collections.singleton("tag1"));
    ResponseEntity<TestPagedModel> response =
        restTemplate
            .withBasicAuth(username, password)
            .exchange(
                "/document-management/search",
                HttpMethod.GET,
                new HttpEntity<>(searchData),
                TestPagedModel.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().getContent().isEmpty());
    assertEquals("testfile", response.getBody().getContent().get(0).name());
  }

  @Test
  @Order(5)
  @DisplayName("Verify document is saved in MinIO")
  void uploadDocument_ShouldVerifyInMinio() throws Exception {
    var filename = "minioverify";
    var data = new NewDocumentData(filename, Collections.singleton("test-tag"));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    byte[] content = "minio test content".getBytes();
    HttpHeaders fileHeaders = new HttpHeaders();
    fileHeaders.setContentType(MediaType.APPLICATION_PDF);
    HttpEntity<ByteArrayResource> fileEntity =
        new HttpEntity<>(
            new ByteArrayResource(content) {
              @Override
              public String getFilename() {
                return filename + ".pdf";
              }
            },
            fileHeaders);

    body.add("file", fileEntity);
    body.add("data", data);

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    ResponseEntity<Void> response =
        restTemplate
            .withBasicAuth(username, password)
            .postForEntity("/document-management/upload", requestEntity, Void.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());

    // Verify in MinIO
    var minioClient = getMinioClient();
    // The StorageService uses userName/fileName as the object path
    var objectPath = username + "/" + filename;
    assertDoesNotThrow(
        () ->
            minioClient.statObject(
                StatObjectArgs.builder().bucket(BUCKET_NAME).object(objectPath).build()));
  }

  // Helper class for PagedModel deserialization in tests
  @Setter
  @Getter
  static class TestPagedModel {
    private List<DocumentData> content;
  }
}
