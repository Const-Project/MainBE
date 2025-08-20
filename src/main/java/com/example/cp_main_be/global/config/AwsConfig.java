package com.example.cp_main_be.global.config; // AwsConfig.java

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AwsConfig {

  // ✅ application.properties의 키 이름과 동일하게 변경
  @Value("${cloudflare.r2.access-key}")
  private String accessKey;

  // ✅ application.properties의 키 이름과 동일하게 변경
  @Value("${cloudflare.r2.secret-key}")
  private String secretKey;

  // ✅ application.properties의 키 이름과 동일하게 변경
  @Value("${cloudflare.r2.endpoint}")
  private String endpoint;

  @Bean
  @Primary
  public AmazonS3 amazonS3() {
    AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

    // R2는 리전 개념이 약하므로 "auto"로 하드코딩하거나,
    // properties에 cloudflare.r2.region 키를 추가한 뒤 @Value로 주입받아도 됩니다.
    String region = "auto";

    return AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .build();
  }
}
