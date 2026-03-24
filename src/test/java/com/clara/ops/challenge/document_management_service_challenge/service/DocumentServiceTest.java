package com.clara.ops.challenge.document_management_service_challenge.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.controller.dto.DocumentData;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.DocumentLocation;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.NewDocumentData;
import com.clara.ops.challenge.document_management_service_challenge.entity.DocumentMetadata;
import com.clara.ops.challenge.document_management_service_challenge.entity.User;
import com.clara.ops.challenge.document_management_service_challenge.exceptions.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

  @Mock private StorageService storageService;
  @Mock private MetadataService metadataService;

  @InjectMocks private DocumentService documentService;

  private User owner;
  private NewDocumentData newDocData;
  private InputStream contentStream;

  @BeforeEach
  void setUp() {
    owner = new User("owner", "pass", "Owner Name");
    newDocData = new NewDocumentData("test.pdf", Collections.singleton("tag1"));
    contentStream = new ByteArrayInputStream("content".getBytes());
  }

  @Test
  @DisplayName("Store new valid document")
  void saveDocument_ShouldSucceed_WhenValidInput() {
    var contentType = "application/pdf";
    var fileSize = 1024L;
    DocumentMetadata metadata =
        new DocumentMetadata(newDocData.name(), fileSize, newDocData.tags(), owner);

    when(metadataService.documentExists(newDocData.name(), owner)).thenReturn(false);
    when(storageService.storeFile(
            anyString(), anyString(), anyLong(), anyString(), any(InputStream.class)))
        .thenReturn(new StorageService.Outcome(true, "success", null));
    when(metadataService.saveMetadata(anyString(), anyLong(), anySet(), any(User.class)))
        .thenReturn(metadata);

    DocumentMetadata result =
        documentService.saveDocument(newDocData, contentStream, contentType, fileSize, owner);

    assertNotNull(result);
    assertEquals(newDocData.name(), result.getName());
    verify(storageService)
        .storeFile(
            eq(owner.getUsername()),
            eq(newDocData.name()),
            eq(fileSize),
            eq(contentType),
            eq(contentStream));
    verify(metadataService)
        .saveMetadata(eq(newDocData.name()), eq(fileSize), eq(newDocData.tags()), eq(owner));
  }

  @Test
  @DisplayName("Invalid content type document")
  void saveDocument_ShouldThrowException_WhenInvalidContentType() {
    var contentType = "text/plain";
    var fileSize = 1024L;

    assertThrows(
        DocumentValidationException.class,
        () ->
            documentService.saveDocument(newDocData, contentStream, contentType, fileSize, owner));
  }

  @Test
  @DisplayName("Document with the same name already exists")
  void saveDocument_ShouldThrowException_WhenDocumentAlreadyExists() {
    var contentType = "application/pdf";
    var fileSize = 1024L;

    when(metadataService.documentExists(newDocData.name(), owner)).thenReturn(true);

    assertThrows(
        DocumentConflictException.class,
        () ->
            documentService.saveDocument(newDocData, contentStream, contentType, fileSize, owner));
  }

  @Test
  @DisplayName("Document exceeds file size limit")
  void saveDocument_ShouldThrowException_WhenFileSizeExceedsLimit() {
    var contentType = "application/pdf";
    var fileSize = 501L * 1024 * 1024; // 501 MB

    assertThrows(
        DocumentSizeExceededException.class,
        () ->
            documentService.saveDocument(newDocData, contentStream, contentType, fileSize, owner));
  }

  @Test
  @DisplayName("Document cannot be stored due to storage error")
  void saveDocument_ShouldThrowException_WhenStorageFails() {
    String contentType = "application/pdf";
    long fileSize = 1024L;

    when(metadataService.documentExists(newDocData.name(), owner)).thenReturn(false);
    when(storageService.storeFile(
            anyString(), anyString(), anyLong(), anyString(), any(InputStream.class)))
        .thenReturn(new StorageService.Outcome(false, "failed", null));

    assertThrows(
        StorageException.class,
        () ->
            documentService.saveDocument(newDocData, contentStream, contentType, fileSize, owner));
  }

  @Test
  @DisplayName("Document location successful retrieval")
  void retrieveLocation_ShouldSucceed_WhenValidInput() {
    long docId = 1L;
    DocumentMetadata metadata =
        new DocumentMetadata("test.pdf", 1024L, Collections.emptySet(), owner);

    when(metadataService.retrieveMetadata(docId)).thenReturn(Optional.of(metadata));
    when(storageService.getDownloadURL(owner.getUsername(), metadata.getName()))
        .thenReturn(new StorageService.Outcome(true, "http://presigned-url", null));
    when(storageService.getExpiryTime()).thenReturn(1);
    when(storageService.getExpiryUnit()).thenReturn(TimeUnit.HOURS);

    DocumentLocation result = documentService.retrieveLocation(docId, owner);

    assertNotNull(result);
    assertEquals("http://presigned-url", result.url());
    assertEquals(1, result.expirationTime());
    assertEquals(TimeUnit.HOURS, result.expirationUnit());
  }

  @Test
  @DisplayName("Document location not found")
  void retrieveLocation_ShouldThrowException_WhenDocumentNotFound() {
    long docId = 1L;
    when(metadataService.retrieveMetadata(docId)).thenReturn(Optional.empty());

    assertThrows(
        DocumentNotFoundException.class, () -> documentService.retrieveLocation(docId, owner));
  }

  @Test
  @DisplayName("Document access not allowed to different owner")
  void retrieveLocation_ShouldThrowException_WhenAccessDenied() {
    long docId = 1L;
    User anotherUser = new User("another", "pass", "Another");
    DocumentMetadata metadata =
        new DocumentMetadata("test.pdf", 1024L, Collections.emptySet(), anotherUser);

    when(metadataService.retrieveMetadata(docId)).thenReturn(Optional.of(metadata));

    assertThrows(
        DocumentAccessException.class, () -> documentService.retrieveLocation(docId, owner));
  }

  @Test
  @DisplayName("Document search successful")
  void searchDocuments_ShouldReturnPage_WhenValidInput() {
    String name = "test";
    Set<String> tags = Collections.singleton("tag1");
    Pageable pageable = PageRequest.of(0, 10);
    DocumentMetadata metadata = new DocumentMetadata("test.pdf", 1024L, tags, owner);
    ReflectionTestUtils.setField(metadata, "id", 1L);
    Page<DocumentMetadata> page = new PageImpl<>(Collections.singletonList(metadata));

    when(metadataService.findDocuments(name, tags, owner, pageable)).thenReturn(page);

    Page<DocumentData> result = documentService.searchDocuments(name, tags, owner, pageable);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals("test.pdf", result.getContent().get(0).name());
  }

  @Test
  @DisplayName("Concurrent threshold reached for more than 10 uploads")
  void saveDocument_ShouldThrowException_WhenConcurrentLimitReached()
      throws InterruptedException, ExecutionException {
    var maxConcurrent = 10;
    var contentType = "application/pdf";
    long fileSize = 1024L;

    // Use a CountDownLatch to make threads wait until all are ready to start saving
    var startLatch = new CountDownLatch(1);
    // Use a CountDownLatch to wait for all threads to finish their work
    var finishLatch = new CountDownLatch(maxConcurrent + 1);

    // Mock storage to pause so we can hit the limit
    when(storageService.storeFile(
            anyString(), anyString(), anyLong(), anyString(), any(InputStream.class)))
        .thenAnswer(
            invocation -> {
              startLatch.await(); // Block here until released
              return new StorageService.Outcome(true, "success", null);
            });

    var executor = Executors.newFixedThreadPool(maxConcurrent + 1);
    List<Future<DocumentMetadata>> futures = new ArrayList<>();

    for (int i = 0; i < maxConcurrent * 2; i++) {
      int index = i;
      futures.add(
          executor.submit(
              () -> {
                try {
                  var data = new NewDocumentData("test" + index + ".pdf", Collections.emptySet());
                  return documentService.saveDocument(
                      data, contentStream, contentType, fileSize, owner);
                } finally {
                  finishLatch.countDown();
                }
              }));
    }

    // Give some time for threads to start and block in storageService.storeFile
    // Since we have maxConcurrent+1 threads, and only 10 can proceed to storeFile as the 11th
    // should fail immediately in DocumentService.saveDocument before calling storageService.

    // Release some capacity by letting threads finish
    startLatch.countDown(); // Release blocked storage calls
    finishLatch.await(5, TimeUnit.SECONDS);

    int exceptionsCount = 0;
    for (var future : futures) {
      try {
        future.get();
      } catch (ExecutionException e) {
        if (e.getCause() instanceof ConcurrentThresholdReachedException) {
          exceptionsCount++;
        }
      }
    }

    assertTrue(
        exceptionsCount > 0,
        "At least one thread should have failed with ConcurrentThresholdReachedException");
    executor.shutdown();
    if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
      executor.shutdownNow();
    }
  }
}
