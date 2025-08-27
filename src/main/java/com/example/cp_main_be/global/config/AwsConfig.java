package com.example.cp_main_be.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

  @Bean
  public S3Client s3Client() {
    // AWS SDK가 EC2 환경임을 자동으로 감지하고,
    // 연결된 IAM 역할(ec2-aws)의 권한을 자동으로 사용합니다.
    // 우리는 리전만 지정해주면 됩니다.
    return S3Client.builder()
        .region(Region.AP_NORTHEAST_2) // 서울 리전
        .build();
  }
}
