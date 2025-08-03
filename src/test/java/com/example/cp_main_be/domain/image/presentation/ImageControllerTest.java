package com.example.cp_main_be.domain.image.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.cp_main_be.domain.image.service.ImageProcessingService;
import com.example.cp_main_be.global.config.WebClientConfig;
import com.example.cp_main_be.global.jwt.JwtTokenProvider;
import com.example.cp_main_be.global.util.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = ImageController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class) // Security 비활성화
@Import(WebClientConfig.class)
@TestPropertySource(
    properties = {"replicate.api.token=test-token", "replicate.api.url=http://localhost:8080"})
class ImageControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ImageProcessingService imageProcessingService;

  @MockBean private JwtTokenProvider jwtTokenProvider;

  @MockBean private UserDetailsService userDetailsService;

  // JWT 관련 MockBean 불필요 (Security가 비활성화되었으므로)

  @Test
  @DisplayName("아바타 생성 요청 성공 시, 200 OK와 함께 PNG 이미지를 반환한다")
  void generateAvatar_success() throws Exception {
    // given
    byte[] fakeImageData = "fake-image-data".getBytes();
    MockMultipartFile mockImageFile =
        new MockMultipartFile("image", "test-image.png", MediaType.IMAGE_PNG_VALUE, fakeImageData);

    given(imageProcessingService.processImageWithAi(any()))
        .willReturn(ApiResponse.success(fakeImageData));

    // when & then (Authorization 헤더 불필요)
    mockMvc
        .perform(multipart("/api/v1/register/upload").file(mockImageFile))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_PNG))
        .andExpect(content().bytes(fakeImageData));
  }

  @Test
  @DisplayName("아바타 생성 요청 실패 시, 500 에러와 함께 실패 응답 JSON을 반환한다")
  void generateAvatar_failure() throws Exception {

    // given
    MockMultipartFile mockImageFile = new MockMultipartFile("image", new byte[0]);

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
