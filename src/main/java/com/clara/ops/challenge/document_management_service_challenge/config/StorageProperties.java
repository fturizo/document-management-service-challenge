package com.clara.ops.challenge.document_management_service_challenge.config;

import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
