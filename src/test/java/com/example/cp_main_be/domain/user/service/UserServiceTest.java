package com.example.cp_main_be.domain.user.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.UserStatus;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.user.dto.request.UserRequest;
import com.example.cp_main_be.domain.member.user.dto.response.UserResponse;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import com.example.cp_main_be.global.jwt.JwtTokenProvider;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private JwtTokenProvider jwtTokenProvider;

  @InjectMocks private UserService userService;

  @DisplayName("회원 생성시 uuid가 자동으로 생기는가")
  @Test
  void 회원_생성시_uuid가_자동으로_생기는가() {
    // given
    User user = User.builder().username("test").build();

    // Simulate the @PrePersist behavior
    doAnswer(
            invocation -> {
              User argUser = invocation.getArgument(0);
              argUser.setUuid(UUID.randomUUID()); // Set UUID on the passed object
              return null; // saveUser is void, so return null
            })
        .when(userRepository)
        .save(any(User.class));

    // when
    userService.saveUser(user);

    // then
    Assertions.assertNotNull(user.getUuid()); // Now the 'user' object should have a UUID
    verify(userRepository).save(any(User.class));
  }

  @DisplayName("유저 이름이 없으면 오류")
  @Test
  void 유저_이름이_없으면_오류() {
    // given
    User user =
        User.builder()
            .email("test@example.com")
            .passwordHash("hashedpassword")
            .level(1L)
            .temperatureScore(0)
            .status(UserStatus.ACTIVE)
            .build();
    doThrow(DataIntegrityViolationException.class).when(userRepository).save(any(User.class));

    // when & then
    Assertions.assertThrows(
        DataIntegrityViolationException.class,
        () -> {
          userService.saveUser(user);
        });
    verify(userRepository).save(any(User.class));
  }

  @DisplayName("유저생성성공")
  @Test
  void 유저생성성공() {
    // given
    Long userId = 1L;
    UUID userUuid = UUID.randomUUID();
    User user = User.builder().id(userId).uuid(userUuid).username("test").build();
    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    // when
    User foundUser = userService.findUserById(userId);

    // then
    Assertions.assertEquals(userId, foundUser.getId());
    Assertions.assertEquals(userUuid, foundUser.getUuid());
    Assertions.assertEquals("test", foundUser.getUsername());
    verify(userRepository).findById(userId);
  }

  @DisplayName("UUID로 유저 조회 성공")
  @Test
  void findUserByUuid_success() {
    // given
    UUID userUuid = UUID.randomUUID();
    User user = User.builder().uuid(userUuid).username("testuser").build();
    given(userRepository.findByUuid(userUuid)).willReturn(Optional.of(user));

    // when
    User foundUser = userService.findUserByUuid(userUuid);

    // then
    Assertions.assertEquals(userUuid, foundUser.getUuid());
    verify(userRepository).findByUuid(userUuid);
  }

  @DisplayName("유저 삭제 성공")
  @Test
  void deleteUser_success() {
    // given
    Long userId = 1L;
    User user = User.builder().id(userId).username("test").build();
    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    // when
    userService.deleteUser(userId);

    // then
    verify(userRepository).findById(userId);
    verify(userRepository).delete(user);
  }

  @DisplayName("유저 삭제 실패 - 유저를 찾을 수 없음")
  @Test
  void deleteUser_fail_userNotFound() {
    // given
    Long userId = 1L;
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    Assertions.assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));
    verify(userRepository).findById(userId);
    verify(userRepository, org.mockito.Mockito.never()).delete(any(User.class));
  }

  @DisplayName("유저 등록 성공 - 새로운 유저")
  @Test
  void registerUser_success_new_user() {
    // given
    UserRequest userRequest = new UserRequest();
    userRequest.setUsername("newuser");
    userRequest.setAvatarUrl("http://example.com/avatar.png");

    User newUser =
        User.builder()
            .id(1L)
            .uuid(UUID.randomUUID())
            .username("newuser")
            .profileImageUrl("http://example.com/avatar.png")
            .build();

    given(userRepository.save(any(User.class))).willReturn(newUser);
    given(jwtTokenProvider.generateAccessToken(any(String.class))).willReturn("testAccessToken");
    given(jwtTokenProvider.generateRefreshToken(any(String.class))).willReturn("testRefreshToken");

    // when
    UserResponse response = userService.registerUser(userRequest);

    // then
    Assertions.assertNotNull(response);
    Assertions.assertEquals("newuser", response.getUsername());
    Assertions.assertEquals("testAccessToken", response.getAccessToken());
    Assertions.assertEquals("testRefreshToken", response.getRefreshToken());
    verify(userRepository).save(any(User.class));
    verify(jwtTokenProvider).generateAccessToken(any(String.class));
    verify(jwtTokenProvider).generateRefreshToken(any(String.class));
  }

  @DisplayName("닉네임 변경 성공")
  @Test
  void updateNickname_success() {
    // given
    Long userId = 1L;
    String oldNickname = "oldname";
    String newNickname = "newname";
    User user = User.builder().id(userId).username(oldNickname).build();
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.save(any(User.class))).willReturn(user);

    // when
    userService.updateNickname(userId, newNickname);

    // then
    Assertions.assertEquals(newNickname, user.getUsername());
    verify(userRepository).findById(userId);
    verify(userRepository).save(user);
  }

  @DisplayName("닉네임 변경 실패 - 유저를 찾을 수 없음")
  @Test
  void updateNickname_fail_userNotFound() {
    // given
    Long userId = 1L;
    String newNickname = "newname";
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    Assertions.assertThrows(
        UserNotFoundException.class, () -> userService.updateNickname(userId, newNickname));
    verify(userRepository).findById(userId);
    verify(userRepository, org.mockito.Mockito.never()).save(any(User.class));
  }

  @DisplayName("아바타 변경 성공")
  @Test
  void updateAvatar_success() {
    // given
    Long userId = 1L;
    String oldAvatarUrl = "http://old.com/avatar.png";
    String newAvatarUrl = "http://new.com/avatar.png";
    User user = User.builder().id(userId).profileImageUrl(oldAvatarUrl).build();
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.save(any(User.class))).willReturn(user);

    // when
    userService.updateAvatar(userId, newAvatarUrl);

    // then
    Assertions.assertEquals(newAvatarUrl, user.getProfileImageUrl());
    verify(userRepository).findById(userId);
    verify(userRepository).save(user);
  }

  @DisplayName("아바타 변경 실패 - 유저를 찾을 수 없음")
  @Test
  void updateAvatar_fail_userNotFound() {
    // given
    Long userId = 1L;
    String newAvatarUrl = "http://new.com/avatar.png";
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    Assertions.assertThrows(
        UserNotFoundException.class, () -> userService.updateAvatar(userId, newAvatarUrl));
    verify(userRepository).findById(userId);
    verify(userRepository, org.mockito.Mockito.never()).save(any(User.class));
  }
}
