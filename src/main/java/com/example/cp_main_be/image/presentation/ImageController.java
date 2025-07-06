package com.example.cp_main_be.image.presentation; // 패키지는 적절히 변경

import com.example.cp_main_be.image.service.ImageProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
public class ImageController {

    private final ImageProcessingService imageProcessingService;

    @PostMapping("/generate-avatar/{userId}")
    public ResponseEntity<byte[]> generateAvatar(
            @PathVariable Long userId,
            @RequestParam("image") MultipartFile imageFile) {

        // 서비스 계층에 이미지 처리 요청을 위임
        byte[] processedImage = imageProcessingService.processImageWithAi(userId, imageFile);

        // 결과 이미지를 byte 배열로 반환
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG) // AI 서버가 PNG로 반환한다고 가정
                .body(processedImage);
    }
}