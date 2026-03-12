package com.example.cp_main_be.global.infra;

import com.example.cp_main_be.domain.avatar.image.ImageUploader;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
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

@Component
@RequiredArgsConstructor
public class S3Uploader implements ImageUploader {

  // SDK v2 클라이언트를 주입받습니다.
  private final S3Client s3Client;

  @Value("${cloudflare.r2.bucket}")
  private String bucket;

  @Override
  public String upload(MultipartFile file, String path) {
    String uniqueFileName = path + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

    try {
      /*
       * 한글 주석:
       * endpoint와 자격증명을 R2로 맞춘 상태이므로
       * 업로드/삭제 대상 버킷도 같은 R2 설정 키를 사용하도록 통일한다.
       */
      PutObjectRequest putObjectRequest =
          PutObjectRequest.builder()
              .bucket(bucket)
              .key(uniqueFileName)
              .contentType(file.getContentType())
              .build();

      s3Client.putObject(
          putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

      return s3Client
          .utilities()
          .getUrl(builder -> builder.bucket(bucket).key(uniqueFileName))
          .toExternalForm();

    } catch (IOException e) {
      throw new CustomApiException(ErrorCode.UPLOAD_FAILED, "파일 업로드에 실패했습니다.");
    }
  }

  @Override
  public void delete(String imageUrl) {
    String key = extractKeyFromUrl(imageUrl);

    try {
      DeleteObjectRequest deleteObjectRequest =
          DeleteObjectRequest.builder().bucket(bucket).key(key).build();

      s3Client.deleteObject(deleteObjectRequest);

    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.UPLOAD_FAILED, "파일 삭제에 실패했습니다.");
    }
  }

  private String extractKeyFromUrl(String imageUrl) {
    try {
      URI uri = URI.create(imageUrl);
      String path = uri.getPath();
      return path.substring(1);
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.INVALID_REQUEST, "잘못된 형식의 URL입니다.");
    }
  }
}
