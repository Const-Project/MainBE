package com.example.cp_main_be.user.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.cp_main_be.avatar.domain.Avatar;
import com.example.cp_main_be.avatar.domain.repository.AvatarRepository;
import com.example.cp_main_be.config.AbstractContainerBaseTest;
import com.example.cp_main_be.user.domain.User;
import com.example.cp_main_be.user.domain.UserStatus;
import com.example.cp_main_be.user.domain.repository.UserRepository;
import com.example.cp_main_be.user.dto.request.UserAvatarRequest;
import com.example.cp_main_be.user.dto.request.UserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.UUID;

import org.assertj.core.api.Assertions;
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

  @Autowired private AvatarRepository avatarRepository;

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

  @DisplayName("아바타 정보를 해당 유저의 아바타 리스트에 저장하고 프로필 아바타를 업데이트한다.")
  @Test
  public void register_myavatar_success() throws Exception {
    //given
    // 1. 테스트 유저 저장
    User testUser = User.builder().username("avatarUser").avatarList(new ArrayList<>()).build(); //
    testUser = userRepository.save(testUser); // DB에 저장하고 영속화된 객체를 받음

    // 2. 테스트 아바타 저장 (사용자 소유 아바타)
    Avatar testAvatar = new Avatar(); //
    testAvatar.setImageUrl("http://example.com/test_avatar.png"); //
    testAvatar.setUser(testUser); // 아바타가 특정 유저에게 속하도록 설정
    testAvatar.setDefaultAvatar(false); //
    testAvatar = avatarRepository.save(testAvatar); // DB에 저장하고 영속화된 객체를 받음

    // 요청 DTO 생성
    UserAvatarRequest request = new UserAvatarRequest(testUser.getId(), testAvatar.getId());
    String requestBody = objectMapper.writeValueAsString(request);

    //when
    ResultActions result =
            mockMvc.perform(
                    post("/api/v1/register/myavatar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody));

    //then
    result
            .andDo(print())
            .andExpect(status().isOk()) // 200 OK 예상
            .andExpect(jsonPath("$.success").value(true)); // 성공 응답 예상
  }

  @DisplayName("아바타 등록 실패 - 유저를 찾을 수 없음")
  @Test
  void register_myavatar_fail_userNotFound() throws Exception {
    // 1. 테스트 데이터 준비 (예: 사용자 및 아바타 생성 및 저장)
    User user = User.builder()
            .username("testuser")
            .uuid(UUID.randomUUID())
            .status(UserStatus.ACTIVE)
            .build();
    user = userRepository.save(user); // 사용자 저장

    Long nonExistentUserId = 1000L;
    Avatar avatar = Avatar.builder()
            .user(user)
            .imageUrl("http://example.com/test-avatar.png")
            .isDefaultAvatar(false)
            .build();
    avatar = avatarRepository.save(avatar); // 아바타 저장

    avatar = avatarRepository.findById(avatar.getId()).orElseThrow(); // 항상 최신 데이터를 가져옴

    // ... (이후 테스트 로직, 예를 들어 존재하지 않는 사용자 ID로 아바타 등록 시도)
    UserAvatarRequest request = new UserAvatarRequest(user.getId() + 999L, avatar.getId()); // 존재하지 않는 사용자 ID
    mockMvc.perform(post("/api/v1/register/myavatar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"))
            .andExpect(jsonPath("$.error.message").value("해당 ID의 사용자를 찾을 수 없습니다 : " + nonExistentUserId));
  }

  @DisplayName("유저의 UUID를 등록한다.")
  @Test
  public void saveUuid_success() throws Exception {
    //given
    User savedUser = userRepository.save(User.builder().username("testUser").build());
    UserRequest userRequest = new UserRequest(UUID.randomUUID(), savedUser.getId(), "testUser");
    String requestBody = objectMapper.writeValueAsString(userRequest);

    //when
    ResultActions result =
            mockMvc.perform(
                    post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody));

    //then
    result
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

    User updatedUser = userRepository.findById(savedUser.getId()).get();
    Assertions.assertThat(updatedUser.getUuid()).isEqualTo(userRequest.getUserUuid());
  }

  @DisplayName("아바타 등록 실패 - 아바타를 찾을 수 없음")
  @Test
  public void register_myavatar_fail_avatarNotFound() throws Exception {
    //given
    // 1. 테스트 유저 저장
    User testUser = User.builder().username("avatarUser").build(); //
    testUser = userRepository.save(testUser); //

    Long nonExistentAvatarId = 999L; // 존재하지 않는 아바타 ID

    UserAvatarRequest request = new UserAvatarRequest(testUser.getId(), nonExistentAvatarId); //
    String requestBody = objectMapper.writeValueAsString(request); //

    //when
    ResultActions result =
            mockMvc.perform(
                    post("/api/v1/register/myavatar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody));

    //then
    result
            .andDo(print())
            .andExpect(status().isBadRequest()) // 400 Bad Request 예상
            .andExpect(jsonPath("$.success").value(false)) // 실패 응답 예상
            .andExpect(jsonPath("$.error.code").value("AVATAR_NOT_FOUND")) // 에러 코드 검증
            .andExpect(jsonPath("$.error.message").value("아바타를 찾을 수 없습니다.")); // 에러 메시지 검증
  }

}
