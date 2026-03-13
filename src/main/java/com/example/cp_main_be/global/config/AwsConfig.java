package com.example.cp_main_be.global.config;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Slf4j
public class AwsConfig {

  @Value("${cloudflare.r2.endpoint}")
  private String endpoint;

  @Value("${cloudflare.r2.access-key}")
  private String accessKey;

  @Value("${cloudflare.r2.secret-key}")
  private String secretKey;

  @Value("${cloudflare.r2.bucket}")
  private String bucket;

  @Bean
  public S3Client s3Client() {
    /*
     * 한글 주석:
     * 일기 이미지는 S3 호환 스토리지(R2)로 업로드하므로
     * application.yml에 선언된 endpoint와 자격증명을 클라이언트에 명시적으로 주입해야 한다.
     */
    log.info("R2 config endpoint={}", endpoint);
    log.info("R2 config bucket={}", bucket);
    log.info(
        "R2 config accessKeyPrefix={}",
        accessKey != null && accessKey.length() >= 4 ? accessKey.substring(0, 4) : "null-or-short");
    log.info("R2 config secretKeyPresent={}", secretKey != null && !secretKey.isBlank());
    log.info("R2 config region={}", Region.AP_NORTHEAST_2);

    return S3Client.builder()
        .endpointOverride(URI.create(endpoint))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .region(Region.AP_NORTHEAST_2)
        .forcePathStyle(true)
        .build();
  }
}
