package com.clara.ops.challenge.document_management_service_challenge;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseIntegrationTest {

  // This container uses the official PostgreSQL Docker image instead of the Bitnami one since it is
  // not compatible with the testcontainer official module
  protected static final PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.4-alpine3.18"))
          .withDatabaseName("testdb")
          .withUsername("testuser")
          .withPassword("testpass");

  protected static final MinIOContainer minio =
      new MinIOContainer(DockerImageName.parse("minio/minio:RELEASE.2025-04-22T22-12-26Z"))
          .withUserName("minioadmin")
          .withPassword("minioadmin");

  protected static final String BUCKET_NAME = "test-bucket";

  static {
    postgres.start();
    minio.start();
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);

    registry.add("app.document.storage.endpoint-url", minio::getS3URL);
    registry.add("app.document.storage.access-key", minio::getUserName);
    registry.add("app.document.storage.secret-key", minio::getPassword);
    registry.add("app.document.storage.bucket-name", () -> BUCKET_NAME);
  }

  protected static MinioClient getMinioClient() {
    return MinioClient.builder()
        .endpoint(minio.getS3URL())
        .credentials(minio.getUserName(), minio.getPassword())
        .build();
  }

  @BeforeAll
  static void initMinioBucket() {
    try {
      var minioClient = getMinioClient();
      if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build())) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize MinIO bucket", e);
    }
  }
}
