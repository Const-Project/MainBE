package com.example.cp_main_be.global.infra;

import com.example.cp_main_be.domain.avatar.image.ImageUploader;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
public class S3Uploader implements ImageUploader {

  // SDK v2 클라이언트를 주입받습니다.
  private final S3Client s3Client;

  // application.yml의 키와 일치시킵니다.
  @Value("${aws.s3.bucket}")
  private String bucket;

  @Override
  public String upload(MultipartFile file, String path) {
    String uniqueFileName = path + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

    try {
      // 1. 업로드 요청 객체 생성
      PutObjectRequest putObjectRequest =
          PutObjectRequest.builder()
              .bucket(bucket)
              .key(uniqueFileName)
              .contentType(file.getContentType())
              .build();

      // 2. 파일 업로드
      s3Client.putObject(
          putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

      // 3. 업로드된 파일의 URL 반환
      return s3Client
          .utilities()
          .getUrl(builder -> builder.bucket(bucket).key(uniqueFileName))
          .toExternalForm();

    } catch (IOException e) {
      throw new IllegalArgumentException("파일 업로드에 실패했습니다: " + e.getMessage());
    }
  }
  /**
   * 바이트 배열을 PNG 파일로 S3에 업로드하고 URL을 반환합니다.
   *
   * @param imageBytes 업로드할 이미지의 바이트 배열
   * @param path       S3 내 파일이 저장될 경로 (예: "images/avatars")
   * @return 업로드된 파일의 S3 URL
   */
  public String uploadByteArrayToPng(byte[] imageBytes, String path) {
    // 고유한 파일명 생성 (확장자는 .png로 고정)
    String uniqueFileName = path + "/" + UUID.randomUUID() + ".png";

    try {
      // 1. PutObjectRequest 생성
      PutObjectRequest putObjectRequest =
              PutObjectRequest.builder()
                      .bucket(bucket)
                      .key(uniqueFileName)
                      .contentType("image/png") // MIME 타입은 PNG로 고정
                      .contentLength((long) imageBytes.length)
                      .build();

      // 2. 바이트 배열을 요청 본문으로 변환하여 파일 업로드
      s3Client.putObject(
              putObjectRequest, RequestBody.fromBytes(imageBytes));

      // 3. 업로드된 파일의 URL 반환
      return s3Client
              .utilities()
              .getUrl(builder -> builder.bucket(bucket).key(uniqueFileName))
              .toExternalForm();

    } catch (S3Exception e) {
      // S3 관련 에러 처리
      throw new RuntimeException("S3 업로드 중 오류 발생: " + e.awsErrorDetails().errorMessage(), e);
    } catch (Exception e) {
      // 일반적인 업로드 실패 에러 처리
      throw new RuntimeException("파일 업로드에 실패했습니다: " + e.getMessage(), e);
    }
  }
  @Override
  public void delete(String imageUrl) {
    // URL에서 파일 경로(key)를 추출
    String key = extractKeyFromUrl(imageUrl);

    try {
      // 1. 삭제 요청 객체 생성
      DeleteObjectRequest deleteObjectRequest =
          DeleteObjectRequest.builder().bucket(bucket).key(key).build();

      // 2. S3에 삭제 요청
      s3Client.deleteObject(deleteObjectRequest);

    } catch (Exception e) {
      throw new RuntimeException("파일 삭제에 실패했습니다: " + e.getMessage());
    }
  }

  private String extractKeyFromUrl(String imageUrl) {
    try {
      // URI 객체를 사용하여 URL의 경로 부분을 안전하게 추출
      URI uri = URI.create(imageUrl);
      String path = uri.getPath();
      // 경로의 맨 앞 '/' 문자 제거
      return path.substring(1);
    } catch (Exception e) {
      throw new IllegalArgumentException("잘못된 형식의 URL입니다: " + imageUrl);
    }
  }
}
