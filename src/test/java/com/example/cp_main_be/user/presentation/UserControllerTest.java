package com.example.cp_main_be.user.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.cp_main_be.config.AbstractContainerBaseTest;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.user.dto.request.UserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest extends AbstractContainerBaseTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  @DisplayName("닉네임을 받아 유저를 등록한다.")
  @Test
  void register() throws Exception {
    // given
    UserRequest userRequest = new UserRequest(UUID.randomUUID(), 1L, "test");
    String requestBody = objectMapper.writeValueAsString(userRequest);

    // when
    ResultActions result =
        mockMvc.perform(
            post("/api/v1/register/nickname")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

    // then
    result
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.username").value("test"))
        .andExpect(jsonPath("$.data.uuid").isNotEmpty());
  }

  @DisplayName("UUID로 유저 정보를 조회한다.")
  @Test
  void getUserInfo_success() throws Exception {
    // given
    // 테스트를 위해 미리 유저를 한 명 저장
    User savedUser = userRepository.save(User.builder().username("existing-user").build());
    UUID userUuid = savedUser.getUuid();

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

    // when
    ResultActions result = mockMvc.perform(get("/api/v1/users/{uuid}", nonExistentUuid));

    // then
    result.andDo(print()).andExpect(status().isNotFound()); // UserNotFoundException이 404로 변환되는지 확인
  }
}
