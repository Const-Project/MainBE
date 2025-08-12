package com.example.cp_main_be.domain.image.presentation;

import com.example.cp_main_be.domain.image.service.ImageProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "이미지 API", description = "이미지 업로드 관련 기능을 제공합니다.")
public class ImageController {

  private final ImageProcessingService imageProcessingService;

  @Operation(summary = "사진 업로드, 아바타 생성", description = "업로드된 사진을 바탕으로 아바타를 png로 생성 후 반환합니다.")
  @PostMapping(value = "/register/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<byte[]> generateAvatar(@RequestParam("image") MultipartFile imageFile) {
    // 성공 시 서비스가 byte[]를 반환. 실패 시 서비스가 예외를 던짐(아래 핸들러가 처리).
    byte[] avatarImage = imageProcessingService.processImageWithAi(imageFile);

    // 성공 응답만 처리
    return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(avatarImage);
  }
}
