package com.example.cp_main_be.domain.user.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.dto.request.UserRequest;
import com.example.cp_main_be.domain.user.dto.response.UserResponse;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest; // SpringBootTest로 변경
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest // SpringBootTest로 변경
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private WebApplicationContext context;

  // MockBean 제거 (SpringBootTest에서는 실제 빈을 사용)

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @DisplayName("유저 등록 및 토큰 발급")
  @Test
  void register() throws Exception {
    // given
    UserRequest userRequest = new UserRequest();
    userRequest.setUsername("testuser");
    userRequest.setAvatarUrl("http://example.com/avatar.png");

    UserResponse userResponse =
        new UserResponse(1L, "testuser", UUID.randomUUID(), "accessToken", "refreshToken");
    given(userService.registerUser(any(UserRequest.class))).willReturn(userResponse);

    String requestBody = objectMapper.writeValueAsString(userRequest);

    // when
    ResultActions result =
        mockMvc.perform(
            post("/api/v1/register").contentType(MediaType.APPLICATION_JSON).content(requestBody));

    // then
    result.andDo(print()).andExpect(status().isOk());
    verify(userService).registerUser(any(UserRequest.class));
  }

  @DisplayName("내 프로필 조회 성공")
  @Test
  void getMyInfo_success() throws Exception {
    // given
    UUID userUuid = UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef");
    User user = User.builder().uuid(userUuid).username("existing-user").build();
    given(userService.findUserByUuid(userUuid)).willReturn(user);

    // when
    ResultActions result = mockMvc.perform(get("/api/v1/users/me").with(user(userUuid.toString())));

    // then
    result
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.username").value("existing-user"))
        .andExpect(jsonPath("$.data.uuid").value(userUuid.toString()));
  }

  @DisplayName("내 프로필 조회 실패 - 유저를 찾을 수 없음")
  @Test
  void getMyInfo_fail_userNotFound() throws Exception {
    // given
    UUID nonExistentUuid = UUID.fromString("fedcba98-7654-3210-fedc-ba9876543210");
    given(userService.findUserByUuid(nonExistentUuid))
        .willThrow(new UserNotFoundException("User not found"));

    // when
    ResultActions result =
        mockMvc.perform(get("/api/v1/users/me").with(user(nonExistentUuid.toString())));

    // then
    result.andDo(print()).andExpect(status().isNotFound());
  }
}
