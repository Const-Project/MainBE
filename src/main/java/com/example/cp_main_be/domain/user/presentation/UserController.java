package com.example.cp_main_be.domain.user.presentation;

import com.example.cp_main_be.domain.avatar.domain.Avatar;
import com.example.cp_main_be.domain.avatar.service.AvatarService;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.dto.request.UserAvatarRequest;
import com.example.cp_main_be.domain.user.dto.request.UserRequest;
import com.example.cp_main_be.domain.user.dto.response.UserResponse;
import com.example.cp_main_be.domain.user.service.UserService;
import com.example.cp_main_be.global.util.ApiResponse;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

  private final UserService userService;
  private final AvatarService avatarService;

  /** 유저 등록 및 토큰 발급 */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<UserResponse>> register(
      @RequestBody @Valid UserRequest userRequest) {
    UserResponse userResponse = userService.registerUser(userRequest);
    return ResponseEntity.ok(ApiResponse.success(userResponse));
  }

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<Void>> saveUuid(@RequestBody @Valid UserRequest userRequest) {
    User user = userService.findUserById(userRequest.getUserId());
    userService.saveUserUuid(user.getId(), userRequest.getUserUuid());

    return ResponseEntity.ok(ApiResponse.success(null));
  }

  /** request를 통해 어떤 유저가 어떤 아바타를 저장하는지 받고, 각 아바타와 유저를 조회해서 user의 avatarList에 추가한다. */
  @PostMapping("/register/myavatar")
  public ResponseEntity<ApiResponse<Void>> registerMyAvatar(
      @RequestBody @Valid UserAvatarRequest request) {
    // 1. 유저 조회
    User findUser = userService.findUserById(request.getUserId());
    if (findUser == null) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.failure("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
    }

    // avatarList가 null일 경우 초기화하는 방어 코드
    if (findUser.getAvatarList() == null) {
      findUser.setAvatarList(new ArrayList<>());
    }

    // 2. 아바타 조회
    Avatar findAvatar = avatarService.findAvatarById(request.getAvatarId());
    if (findAvatar == null) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.failure("AVATAR_NOT_FOUND", "아바타를 찾을 수 없습니다."));
    }

    // 레벨에 안맞으면 아바타 추가 못함, 추후 기준에 따라 달라질 듯

    // 레벨에 맞으면 아바타 리스트에 넣을 수 있다. (최대 3개)
    findUser.getAvatarList().add(findAvatar);
    userService.saveUser(findUser);

    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @DeleteMapping("/users/me")
  public ResponseEntity<ApiResponse<Void>> deleteUser() {
    String userUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User user = userService.findUserByUuid(UUID.fromString(userUuid));
    userService.deleteUser(user.getId());
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PatchMapping("/users/me/avatar")
  public ResponseEntity<ApiResponse<Void>> updateAvatar(
      @RequestBody @Valid AvatarChangeRequest request) {
    String userUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User user = userService.findUserByUuid(UUID.fromString(userUuid));
    userService.updateAvatar(user.getId(), request.getNewAvatarUrl());
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PatchMapping("/users/me/nickname")
  public ResponseEntity<ApiResponse<Void>> updateNickname(
      @RequestBody @Valid NicknameChangeRequest request) {
    String userUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User user = userService.findUserByUuid(UUID.fromString(userUuid));
    userService.updateNickname(user.getId(), request.getNewNickname());
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @GetMapping("/users/me")
  public ResponseEntity<ApiResponse<UserResponse>> getMyInfo() {
    // SecurityContextHolder에서 현재 인증된 사용자(UUID)를 가져옴
    String uuid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User user = userService.findUserByUuid(UUID.fromString(uuid));
    UserResponse userResponse =
        new UserResponse(
            user.getId(), user.getUsername(), user.getUuid(), null, null); // 토큰은 응답에 포함하지 않음
    return ResponseEntity.ok(ApiResponse.success(userResponse));
  }
}
