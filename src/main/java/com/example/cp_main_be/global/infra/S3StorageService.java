package com.example.cp_main_be.global.infra;

import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

  private final S3Client s3Client;

  @Value("${cloudflare.r2.bucket}")
  private String bucket;

  @Override
  public String uploadFile(byte[] fileBytes, String folderPath, String originalFileName) {
    if (fileBytes == null || fileBytes.length == 0) {
      throw new CustomApiException(ErrorCode.INVALID_FILE);
    }

    // 1. 파일의 고유한 이름 생성 (파일 충돌 방지)
    String uniqueFileName = createUniqueFileName(originalFileName);
    String objectKey = folderPath + uniqueFileName; // 예: "avatars/uuid-image.png"

    try {
      // 2. S3에 업로드할 요청 객체 생성
      PutObjectRequest putObjectRequest =
          PutObjectRequest.builder()
              .bucket(bucket)
              .key(objectKey)
              .contentType(getContentType(originalFileName)) // 파일 확장자에 맞는 Content-Type 설정
              .build();

      // 3. [수정] S3 클라이언트를 통해 파일 업로드 (byte[]를 직접 사용)
      s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));

      // 4. 업로드된 파일의 URL 반환
      return s3Client
          .utilities()
          .getUrl(builder -> builder.bucket(bucket).key(objectKey))
          .toExternalForm();

    } catch (Exception e) {
      // 업로드 중 에러 발생 시
      throw new CustomApiException(ErrorCode.UPLOAD_FAILED);
    }
  }

  /** 원본 파일 이름에서 확장자를 추출하고, UUID를 결합하여 고유한 파일 이름을 생성합니다. */
  private String createUniqueFileName(String originalFileName) {
    String extension = "";
    if (originalFileName != null && originalFileName.contains(".")) {
      extension = originalFileName.substring(originalFileName.lastIndexOf("."));
    }
    return UUID.randomUUID().toString() + extension;
  }

  /** 파일 이름의 확장자를 기반으로 적절한 Content-Type을 반환합니다. */
  private String getContentType(String fileName) {
    if (fileName != null && fileName.contains(".")) {
      String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
      switch (extension) {
        case ".png":
          return "image/png";
        case ".jpg":
        case ".jpeg":
          return "image/jpeg";
        case ".gif":
          return "image/gif";
        default:
          return "application/octet-stream"; // 알 수 없는 경우 기본값
      }
    }
    return "application/octet-stream";
  }
}
