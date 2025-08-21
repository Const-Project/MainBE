package com.example.cp_main_be.domain.member.user.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.dto.request.AvatarChangeRequest;
import com.example.cp_main_be.domain.member.user.dto.request.NicknameChangeRequest;
import com.example.cp_main_be.domain.member.user.dto.request.UserRegisterRequest;
import com.example.cp_main_be.domain.member.user.dto.response.UserResponse;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "유저 API", description = "회원 관련 기능을 제공합니다.")
public class UserController {

  private final UserService userService;

  @Operation(summary = "유저 등록 및 토큰 발급", description = "유저가 가입하고 액세스+리프레시 토큰을 반환합니다")
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<UserResponse>> register(
      @RequestBody @Valid UserRegisterRequest userRequest) {
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

  @Operation(summary = "회원 탈퇴", description = "서비스에서 탈퇴합니다")
  @DeleteMapping("/users/me")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@AuthenticationPrincipal User user) {
    userService.deleteUser(user.getId());
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "아바타 수정", description = "새로운 아바타 정보로 업데이트합니다")
  @PatchMapping("/users/me/avatar")
  public ResponseEntity<ApiResponse<Void>> updateAvatar(
      @AuthenticationPrincipal User user, @RequestBody @Valid AvatarChangeRequest request) {

    userService.updateAvatar(user.getId(), request.getNewAvatarUrl());
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "유저 닉네임 수정", description = "유저 닉네임을 수정합니다")
  @PatchMapping("/users/me/nickname")
  public ResponseEntity<ApiResponse<Void>> updateNickname(
      @AuthenticationPrincipal User user, @RequestBody @Valid NicknameChangeRequest request) {
    userService.updateNickname(user.getId(), request.getNewNickname());
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "내 정보 조회", description = "내 정보를 조회합니다")
  @GetMapping("/users/me")
  public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(@AuthenticationPrincipal User user) {
    // SecurityContextHolder에서 현재 인증된 사용자(UUID)를 가져옴
    UserResponse userResponse =
        new UserResponse(
            user.getId(), user.getUsername(), user.getUuid(), null, null); // 토큰은 응답에 포함하지 않음
    return ResponseEntity.ok(ApiResponse.success(userResponse));
  }

  @GetMapping("/level")
  @Operation(summary = "점수 및 레벨 조회")
  public ResponseEntity<ApiResponse<UserResponse.LevelStatusResponseDTO>> getLevel(
      @AuthenticationPrincipal User user) {
    UserResponse.LevelStatusResponseDTO levelStatusResponseDTO = userService.getLevel(user);
    return ResponseEntity.ok(ApiResponse.success(levelStatusResponseDTO));
  }
}
