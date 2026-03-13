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
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Configuration
@Slf4j
public class AwsConfig {
  private static final Region R2_REGION = Region.of("auto");

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
    log.info("R2 config region={}", R2_REGION);

    S3Client s3Client =
        S3Client.builder()
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
            .region(R2_REGION)
            .forcePathStyle(true)
            .build();

    try {
      ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
      log.info(
          "R2 listBuckets success bucketCount={}, buckets={}",
          listBucketsResponse.buckets() != null ? listBucketsResponse.buckets().size() : 0,
          listBucketsResponse.buckets() != null
              ? listBucketsResponse.buckets().stream().map(bucket -> bucket.name()).toList()
              : java.util.List.of());
    } catch (S3Exception e) {
      log.error(
          "R2 listBuckets failed statusCode={}, errorCode={}, requestId={}, extendedRequestId={}, message={}",
          e.statusCode(),
          e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : null,
          e.requestId(),
          e.extendedRequestId(),
          e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage(),
          e);
    } catch (Exception e) {
      log.error(
          "R2 listBuckets failed exceptionType={}, message={}",
          e.getClass().getName(),
          e.getMessage(),
          e);
    }

    try {
      s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
      log.info("R2 headBucket success bucket={}", bucket);
    } catch (S3Exception e) {
      log.error(
          "R2 headBucket failed bucket={}, statusCode={}, errorCode={}, requestId={}, extendedRequestId={}, message={}",
          bucket,
          e.statusCode(),
          e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : null,
          e.requestId(),
          e.extendedRequestId(),
          e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage(),
          e);
    } catch (Exception e) {
      log.error(
          "R2 headBucket failed bucket={}, exceptionType={}, message={}",
          bucket,
          e.getClass().getName(),
          e.getMessage(),
          e);
    }

    return s3Client;
  }
}
