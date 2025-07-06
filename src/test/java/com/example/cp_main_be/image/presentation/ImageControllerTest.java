package com.example.cp_main_be.image.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.cp_main_be.image.service.ImageProcessingService;
import com.example.cp_main_be.util.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ImageController.class) // ImageController만 테스트
class ImageControllerTest {

  @Autowired private MockMvc mockMvc; // HTTP 요청을 흉내내는 객체

  @MockBean private ImageProcessingService imageProcessingService; // 서비스는 가짜 객체로 대체

  @Test
  @DisplayName("아바타 생성 요청 성공 시, 200 OK와 함께 PNG 이미지를 반환한다")
  void generateAvatar_success() throws Exception {
    // given (준비)
    byte[] fakeImageData = "fake-image-data".getBytes();
    MockMultipartFile mockImageFile =
        new MockMultipartFile("image", "test-image.png", MediaType.IMAGE_PNG_VALUE, fakeImageData);

    // 서비스가 성공적으로 ApiResponse를 반환한다고 가정
    given(imageProcessingService.processImageWithAi(any()))
        .willReturn(ApiResponse.success(fakeImageData));

    // when & then (실행 및 검증)
    mockMvc
        .perform(multipart("/api/v1/register/upload").file(mockImageFile))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_PNG))
        .andExpect(content().bytes(fakeImageData));
  }

  @Test
  @DisplayName("아바타 생성 요청 실패 시, 500 에러와 함께 실패 응답 JSON을 반환한다")
  void generateAvatar_failure() throws Exception {
    // given
    MockMultipartFile mockImageFile = new MockMultipartFile("image", new byte[0]);

    // 서비스가 실패 ApiResponse를 반환한다고 가정
    given(imageProcessingService.processImageWithAi(any()))
        .willReturn(ApiResponse.failure("AI_ERROR", "AI 서버 처리 중 오류 발생"));

    // when & then
    mockMvc
        .perform(multipart("/api/v1/register/upload").file(mockImageFile))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("AI_ERROR"))
        .andExpect(jsonPath("$.error.message").value("AI 서버 처리 중 오류 발생"));
  }
}
