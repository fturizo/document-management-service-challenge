package com.clara.ops.challenge.document_management_service_challenge.config;

import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * The StorageProperties class is used to configure and manage the storage-related properties for
 * the application's document management system. These properties are defined under the prefix
 * "app.document.storage". <br>
 * This class provides the necessary configuration for integrating with an external cloud storage
 * provider, such as MinIO or AWS S3, by supplying key details for bucket name, endpoint URL, access
 * credentials, and expiration settings for signed URLs. <br>
 * <br>
 * Fields:
 *
 * <ul>
 *   <li>bucketName: The name of the Minio bucket used for storing documents.
 *   <li>endpointUrl: The Minio service endpoint URL for the storage configuration.
 *   <li>accessKey: The access key for authenticating with the storage service via Minio.
 *   <li>secretKey: The secret key associated with the access key for secure communication.
 *   <li>expirationTime: The default expiration time for pre-signed URLs.
 *   <li>expirationUnit: The time unit associated with the expiration time.
 * </ul>
 */
@Configuration
@ConfigurationProperties("app.document.storage")
@Getter
@Setter
public class StorageProperties {

  private String bucketName;
  private String endpointUrl;
  private String accessKey;
  private String secretKey;
  private Integer expirationTime;
  private TimeUnit expirationUnit;
}
