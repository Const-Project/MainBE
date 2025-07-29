package com.example.cp_main_be.domain.user.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.dto.request.UserRequest;
import com.example.cp_main_be.domain.user.service.UserService;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(UserController.class)
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private UserService userService;

  @DisplayName("닉네임을 받아 유저를 등록한다.")
  @Test
  void register() throws Exception {
    // given
    UserRequest userRequest = new UserRequest(null, null, "test");
    User user = User.builder().username("test").build();
    doNothing().when(userService).saveUser(any(User.class));

    String requestBody = objectMapper.writeValueAsString(userRequest);

    // when
    ResultActions result =
        mockMvc.perform(
            post("/api/v1/register/nickname")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

    // then
    result.andDo(print()).andExpect(status().isOk());
    verify(userService).saveUser(any(User.class));
  }

  @DisplayName("UUID로 유저 정보를 조회한다.")
  @Test
  void getUserInfo_success() throws Exception {
    // given
    UUID userUuid = UUID.randomUUID();
    User user = User.builder().uuid(userUuid).username("existing-user").build();
    given(userService.findUserByUuid(userUuid)).willReturn(user);

    // when
    ResultActions result = mockMvc.perform(get("/api/v1/users/{uuid}", userUuid));

    // then
    result
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.username").value("existing-user"))
        .andExpect(jsonPath("$.data.uuid").value(userUuid.toString()));
  }

  @DisplayName("존재하지 않는 UUID로 유저 정보를 조회하면 404 에러가 발생한다.")
  @Test
  void getUserInfo_fail_whenUserNotFound() throws Exception {
    // given
    UUID nonExistentUuid = UUID.randomUUID();
    given(userService.findUserByUuid(nonExistentUuid))
        .willThrow(new UserNotFoundException("User not found"));

    // when
    ResultActions result = mockMvc.perform(get("/api/v1/users/{uuid}", nonExistentUuid));

    // then
    result.andDo(print()).andExpect(status().isNotFound());
  }
}
