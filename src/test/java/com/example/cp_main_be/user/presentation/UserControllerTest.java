package com.example.cp_main_be.user.presentation;

import com.example.cp_main_be.domain.avatar.domain.Avatar;
import com.example.cp_main_be.domain.avatar.domain.repository.AvatarRepository;
import com.example.cp_main_be.domain.avatar.service.AvatarService;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.user.dto.request.UserAvatarRequest;
import com.example.cp_main_be.domain.user.dto.request.UserRequest;
import com.example.cp_main_be.domain.user.presentation.UserController;
import com.example.cp_main_be.domain.user.service.UserService;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private UserService userService;

  @MockBean private AvatarService avatarService;

  @MockBean private UserRepository userRepository;

  @MockBean private AvatarRepository avatarRepository;

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

  @DisplayName("아바타 정보를 해당 유저의 아바타 리스트에 저장하고 프로필 아바타를 업데이트한다.")
  @Test
  void register_myavatar_success() throws Exception {
    // given
    User testUser = User.builder().id(1L).username("avatarUser").avatarList(new ArrayList<>()).build();
    Avatar testAvatar = Avatar.builder().id(1L).imageUrl("http://example.com/test_avatar.png").user(testUser).isDefaultAvatar(false).build();

    // Mock the service calls that the controller will make
    // Assuming userService.findUserById and avatarService.findAvatarById are called
    given(userService.findUserById(testUser.getId())).willReturn(testUser);
    given(avatarService.findAvatarById(testAvatar.getId())).willReturn(testAvatar);

    // Request DTO creation
    UserAvatarRequest request = new UserAvatarRequest(testUser.getId(), testAvatar.getId());
    String requestBody = objectMapper.writeValueAsString(request);

    // when
    ResultActions result =
            mockMvc.perform(
                    post("/api/v1/register/myavatar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody));

    // then
    result
            .andDo(print())
            .andExpect(status().isOk()) // Expect 200 OK
            .andExpect(jsonPath("$.success").value(true)); // Expect success response

    // Verify that the service methods were called
    verify(userService).findUserById(testUser.getId());
    verify(avatarService).findAvatarById(testAvatar.getId());
  }

  @DisplayName("아바타 등록 실패 - 유저를 찾을 수 없음")
  @Test
  void register_myavatar_fail_userNotFound() throws Exception {
    // given
    Long nonExistentUserId = 1000L;
    Long dummyAvatarId = 1L;

    given(userService.findUserById(nonExistentUserId))
            .willThrow(new UserNotFoundException("해당 ID의 사용자를 찾을 수 없습니다 : " + nonExistentUserId));

    UserAvatarRequest request = new UserAvatarRequest(nonExistentUserId, dummyAvatarId);
    String requestBody = objectMapper.writeValueAsString(request);

    // when
    ResultActions result = mockMvc.perform(post("/api/v1/register/myavatar")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody));

    // then
    // 1. HTTP 상태 코드가 404 Not Found인지 확인합니다.
    result.andDo(print())
            .andExpect(status().isNotFound())
            // 2. 응답 JSON의 'success' 필드가 false인지 확인합니다.
            .andExpect(jsonPath("$.success").value(false))
            // 3. 에러 코드가 "USER_NOT_FOUND"인지 확인합니다.
            .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"))
            // 4. 에러 메시지가 예상과 일치하는지 확인합니다.
            .andExpect(jsonPath("$.error.message").value("해당 ID의 사용자를 찾을 수 없습니다 : " + nonExistentUserId));

    verify(userService).findUserById(nonExistentUserId);
  }

  @DisplayName("유저의 UUID를 등록한다.")
  @Test
  void saveUuid_success() throws Exception {
    // given
    Long testUserId = 1L;
    UUID testUserUuid = UUID.randomUUID();
    String testUsername = "testUser";

    User foundUser = User.builder()
            .id(testUserId)
            .username(testUsername)
            .build();
    given(userService.findUserById(testUserId)).willReturn(foundUser);

    // 2. userService.saveUserUuid() 목킹:
    // 컨트롤러가 userService.saveUserUuid(user.getId(), userRequest.getUserUuid())를 호출할 때,
    // 이 메서드는 void 타입이므로 아무것도 하지 않도록 설정
    doNothing().when(userService).saveUserUuid(any(Long.class), any(UUID.class));

    UserRequest userRequest = new UserRequest(testUserUuid, testUserId, testUsername);
    String requestBody = objectMapper.writeValueAsString(userRequest);

    // when
    ResultActions result =
            mockMvc.perform(
                    post("/api/v1/register") // User UUID를 등록하는 엔드포인트
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody));

    // then
    result
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

    verify(userService).findUserById(testUserId);
    verify(userService).saveUserUuid(testUserId, testUserUuid);
  }

  @DisplayName("아바타 등록 실패 - 아바타를 찾을 수 없음")
  @Test
  void register_myavatar_fail_avatarNotFound() throws Exception {
    // given
    Long testUserId = 1L; // 가상의 사용자 ID
    Long nonExistentAvatarId = 999L; // 존재하지 않는 아바타 ID

    // 1. userService.findUserById() 목킹:
    // 컨트롤러가 유저를 먼저 찾으므로, 이 테스트에서는 유저가 존재한다고 가정하고 유효한 User 객체를 반환하도록 함
    User foundUser = User.builder().id(testUserId).username("testUser").avatarList(new ArrayList<>()).build();
    given(userService.findUserById(testUserId)).willReturn(foundUser);

    // 2. avatarService.findAvatarById() 목킹:
    // 컨트롤러가 아바타를 찾을 때, 아바타가 존재하지 않는 상황을 시뮬레이션하기 위해 null을 반환하도록 함
    given(avatarService.findAvatarById(nonExistentAvatarId)).willReturn(null);

    // 요청 DTO를 생성
    UserAvatarRequest request = new UserAvatarRequest(testUserId, nonExistentAvatarId);
    String requestBody = objectMapper.writeValueAsString(request);

    // when
    ResultActions result =
            mockMvc.perform(
                    post("/api/v1/register/myavatar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody));

    // then
    result
            .andDo(print())
            .andExpect(status().isBadRequest()) // 컨트롤러에서 badRequest를 반환하므로 400 예상
            .andExpect(jsonPath("$.success").value(false)) // 실패 응답 예상
            .andExpect(jsonPath("$.error.code").value("AVATAR_NOT_FOUND")) // 에러 코드 검증
            .andExpect(jsonPath("$.error.message").value("아바타를 찾을 수 없습니다.")); // 에러 메시지 검증

    // verify
    verify(userService).findUserById(testUserId);
    verify(avatarService).findAvatarById(nonExistentAvatarId);
    // 아바타를 찾지 못했으므로 userService.saveUser는 호출되지 않아야 함
    verify(userService, org.mockito.Mockito.never()).saveUser(any(User.class));
  }

}
