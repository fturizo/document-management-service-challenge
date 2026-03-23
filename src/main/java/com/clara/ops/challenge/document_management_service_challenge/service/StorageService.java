package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.config.StorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StorageService {

  private final MinioClient minioClient;
  private final String bucketName;

  public StorageService(StorageProperties properties) {
    log.info(
        "Initializing Storage Service using Minio Client with endpoint URL: {} and bucket: {}",
        properties.getEndpointUrl(),
        properties.getBucketName());
    this.bucketName = properties.getBucketName();
    this.minioClient =
        MinioClient.builder()
            .endpoint(properties.getEndpointUrl())
            .credentials(properties.getAccessKey(), properties.getSecretKey())
            .build();
  }

  public Outcome verifyBucketExists(String bucketName) {
    var arguments = BucketExistsArgs.builder().bucket(bucketName).build();
    try {
      return minioClient.bucketExists(arguments)
          ? success()
          : error("Bucket %s doesn't exist".formatted(bucketName));
    } catch (ErrorResponseException ex) {
      var errorResponse = ex.errorResponse();
      return error(
          "Bucket verification via Minio client encountered expected error: [%s] - %s"
              .formatted(errorResponse.code(), errorResponse.message()));
    } catch (InsufficientDataException ex) {
      return error(
          "Bucket verification via Minio client not possible due to insufficient data: %s"
              .formatted(ex.getLocalizedMessage()));
    } catch (Exception ex) {
      return error("Bucket verification via Minio client encountered an internal error", ex);
    }
  }

  public Outcome storeFile(
      String userName, String fileName, long fileSize, String contentType, InputStream contents) {
    var verificationOutcome = verifyBucketExists(bucketName);
    if (!verificationOutcome.success()) {
      return verificationOutcome;
    }
    var arguments =
        PutObjectArgs.builder().bucket(bucketName).stream(contents, fileSize, -1)
            .object(String.format("%s/%s", userName, fileName))
            .contentType(contentType)
            .build();
    try {
      log.debug(
          "Uploading file to Minio bucket: {} - {} - [{}]", bucketName, fileName, contentType);
      var response = minioClient.putObject(arguments);
      log.debug(
          "Successfully uploaded object {} via Minio client to S3 storage", response.object());
      return success();
    } catch (ErrorResponseException ex) {
      var errorResponse = ex.errorResponse();
      return error(
          "Object upload via Minio client encountered expected error: [%s] - %s"
              .formatted(errorResponse.code(), errorResponse.message()));
    } catch (InsufficientDataException ex) {
      return error(
          "Object upload via Minio client not possible due to insufficient data: %s"
              .formatted(ex.getLocalizedMessage()));
    } catch (IOException ex) {
      return error(
          "Object upload via Minio client encountered IO error: %s"
              .formatted(ex.getLocalizedMessage()));
    } catch (Exception ex) {
      return error("Object upload encountered an internal server error", ex);
    }
  }

  private static Outcome success() {
    return new Outcome(true, "Storage operation successful", null);
  }

  private static Outcome error(String errorMessage) {
    return new Outcome(false, errorMessage, null);
  }

  private static Outcome error(String errorMessage, Throwable throwable) {
    return new Outcome(false, errorMessage, throwable);
  }

  public record Outcome(boolean success, String message, Throwable error) {}
}
