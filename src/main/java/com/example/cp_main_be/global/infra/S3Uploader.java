package com.example.cp_main_be.global.infra;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.cp_main_be.domain.garden.image.ImageUploader;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class S3Uploader implements ImageUploader {

  private final AmazonS3 amazonS3;

  @Value("${cloudflare.r2.bucket}")
  private String bucket;

  @Override
  public String upload(MultipartFile file, String path) {
    String fileName = path + "/" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(file.getSize());
    metadata.setContentType(file.getContentType());

    try {
      amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);
    } catch (IOException e) {
      throw new IllegalArgumentException("파일 업로드에 실패했습니다.");
    }
    return amazonS3.getUrl(bucket, fileName).toString();
  }

  @Override
  public void delete(String imageUrl) {
    // S3 URL에서 파일 경로(key)를 추출
    String key = extractKeyFromUrl(imageUrl);

    try {
      // S3에 삭제 요청을 보냄
      amazonS3.deleteObject(new DeleteObjectRequest(bucket, key));
    } catch (Exception e) {
      throw new RuntimeException("파일 삭제에 실패했습니다: " + e.getMessage());
    }
  }

  private String extractKeyFromUrl(String imageUrl) {
    // URL에서 버킷 이름과 경로를 제외한 부분 추출
    String bucketUrl = amazonS3.getUrl(bucket, "").toString();
    // URL 인코딩된 문자열을 처리하기 위해 replaceAll 사용
    String key = imageUrl.replaceFirst(bucketUrl, "");
    // 경로의 첫 번째 슬래시 제거
    if (key.startsWith("/")) {
      key = key.substring(1);
    }
    return key;
  }
}
