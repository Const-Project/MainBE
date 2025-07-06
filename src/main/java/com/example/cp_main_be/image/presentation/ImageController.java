package com.example.cp_main_be.image.presentation; // 패키지는 적절히 변경

import com.example.cp_main_be.image.service.ImageProcessingService;
import com.example.cp_main_be.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ImageController {

  private final ImageProcessingService imageProcessingService;

  @PostMapping("/register/upload")
  public ResponseEntity<?> generateAvatar(@RequestParam("image") MultipartFile imageFile) {

    ApiResponse<byte[]> response = imageProcessingService.processImageWithAi(imageFile);

    // 서비스가 반환한 결과가 성공인지 확인
    if (response.isSuccess()) {
      // 성공 시: 200 OK 상태 코드와 함께 이미지 데이터(byte[])를 직접 본문에 담아 반환
      return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(response.getData());
    } else {
      // 실패 시: 500 서버 에러 상태 코드와 함께 ApiResponse 객체(에러 정보 포함)를 본문에 담아 반환
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }
}
