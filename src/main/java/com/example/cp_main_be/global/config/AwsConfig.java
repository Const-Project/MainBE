// AwsConfig.java 또는 R2Config.java
package com.example.cp_main_be.global.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig { // 파일 이름은 그대로 두셔도 됩니다.

  @Value("${r2.account-id}")
  private String accountId;

  @Value("${r2.access-key}")
  private String accessKey;

  @Value("${r2.secret-key}")
  private String secretKey;

  @Bean
  public S3Client s3Client() {
    // R2 접속을 위한 엔드포인트 URL 생성
    String endpoint = String.format("https://%s.r2.cloudflarestorage.com", accountId);

    // R2 인증 정보 설정 (SDK v2 방식)
    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
    StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

    // S3 클라이언트 빌드 (SDK v2 방식)
    return S3Client.builder()
        .endpointOverride(URI.create(endpoint)) // R2 엔드포인트 지정
        .region(Region.of("auto")) // R2는 리전이 없으므로 'auto'
        .credentialsProvider(credentialsProvider)
        .build();
  }
}
