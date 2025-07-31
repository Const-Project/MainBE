package com.example.cp_main_be.domain.user.presentation;

import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.dto.request.AvatarChangeRequest;
import com.example.cp_main_be.domain.user.dto.request.NicknameChangeRequest;
import com.example.cp_main_be.domain.user.dto.request.UserRequest;
import com.example.cp_main_be.domain.user.dto.response.UserResponse;
import com.example.cp_main_be.domain.user.service.UserService;
import com.example.cp_main_be.global.util.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

  private final UserService userService;

  /** 유저 등록 및 토큰 발급 */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<UserResponse>> register(
      @RequestBody @Valid UserRequest userRequest) {
    UserResponse userResponse = userService.registerUser(userRequest);
    return ResponseEntity.ok(ApiResponse.success(userResponse));
  }

  //    @DeleteMapping("/register/nickname")
  //    public ResponseEntity<Void> delete(@RequestBody @Valid UserRequest userRequest) {
  //        User user = userService.findUserById(userRequest.getUserId());
  //
  //        userService.deleteUser(user.getId());
  //        return ResponseEntity.ok().build();
  //    }

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
