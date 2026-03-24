package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.config.StorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.http.Method;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * A service for managing file storage operations with MinIO client integration. Provides
 * functionalities such as bucket verification, file storage, and generating presigned URLs for
 * download.
 */
@Service
@Slf4j
public class StorageService {

  private final MinioClient minioClient;
  private final String bucketName;

  @Getter private final int expiryTime;
  @Getter private final TimeUnit expiryUnit;

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
    this.expiryTime = properties.getExpirationTime();
    this.expiryUnit = properties.getExpirationUnit();
  }

  private static String filePathOf(String userName, String fileName) {
    return String.format("%s/%s", userName, fileName);
  }

  /**
   * Verifies if the specified bucket exists in the MinIO storage system.
   *
   * @param bucketName the name of the bucket to verify
   * @return an {@code Outcome} indicating success if the bucket exists, or an error with an
   *     appropriate message otherwise
   */
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

  /**
   * Stores a file in the specified MinIO bucket. This method verifies if the bucket exists before
   * attempting to store the file. If the bucket doesn't exist or an error occurs during the upload
   * process, an appropriate error message is returned.
   *
   * @param userName the name of the user to associate with the file into its final path
   * @param fileName the name of the file to be stored
   * @param fileSize the size of the file in bytes
   * @param contentType the MIME type of the file
   * @param contents the input stream containing the file's data
   * @return an {@code Outcome} indicating success if the file is successfully uploaded, or an error
   *     with an appropriate message otherwise
   */
  public Outcome storeFile(
      String userName, String fileName, long fileSize, String contentType, InputStream contents) {
    var verificationOutcome = verifyBucketExists(bucketName);
    if (!verificationOutcome.success()) {
      return verificationOutcome;
    }
    var arguments =
        PutObjectArgs.builder().bucket(bucketName).stream(contents, fileSize, -1)
            .object(filePathOf(userName, fileName))
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

  /**
   * Generates a MinIO pre-signed URL that can be used to download a previously stored document
   * file.
   *
   * @param userName The username of the user that owns the document.
   * @param fileName The name of the file to locate.
   * @return an {@code Outcome} indicating success if the download URL has been successfully
   *     generated, or an error with an appropriate message otherwise. In case of success the
   *     outcome's <code>message</code> property will contain the value of the pre-signed URL.
   */
  public Outcome getDownloadURL(String userName, String fileName) {
    var arguments =
        GetPresignedObjectUrlArgs.builder()
            .bucket(bucketName)
            .object(filePathOf(userName, fileName))
            .expiry(expiryTime, expiryUnit)
            .method(Method.GET)
            .build();
    try {
      var presignedURL = this.minioClient.getPresignedObjectUrl(arguments);
      return success(presignedURL);
    } catch (ErrorResponseException ex) {
      var errorResponse = ex.errorResponse();
      return error(
          "Generating Presigned URL via MinIO Client for object not possible: [%s] - %s"
              .formatted(errorResponse.code(), errorResponse.message()));
    } catch (InsufficientDataException ex) {
      return error(
          "Generating Presigned URL via MinIO Client for object not possible due to insufficient data: %s"
              .formatted(ex.getLocalizedMessage()));
    } catch (Exception ex) {
      return error(
          "Generating Presigned URL via MinIO Client encountered an internal server error", ex);
    }
  }

  private static Outcome success() {
    return new Outcome(true, "", null);
  }

  private static Outcome success(String value) {
    return new Outcome(true, value, null);
  }

  private static Outcome error(String errorMessage) {
    return new Outcome(false, errorMessage, null);
  }

  private static Outcome error(String errorMessage, Throwable throwable) {
    return new Outcome(false, errorMessage, throwable);
  }

  /**
   * A utility record class used to hold the outcome of a public storage operation.
   *
   * @param success Indicates if the operation was successful or not.
   * @param message Holds either context information in the case of success or an error message in
   *     case of failure.
   * @param error In case of failure it MAY contain the {@link Throwable} source of an error that
   *     caused the failure.
   */
  public record Outcome(boolean success, String message, Throwable error) {}
}
