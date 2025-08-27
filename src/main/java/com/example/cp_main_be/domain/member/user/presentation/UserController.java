package com.example.cp_main_be.domain.member.user.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.user.dto.request.AvatarChangeRequest;
import com.example.cp_main_be.domain.member.user.dto.request.NicknameChangeRequest;
import com.example.cp_main_be.domain.member.user.dto.response.UserProfileResponse;
import com.example.cp_main_be.domain.member.user.dto.response.UserRegisterResponse;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.global.common.ApiResponse;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "유저 API", description = "회원 관련 기능을 제공합니다.")
public class UserController {

  private final UserService userService;
  private final UserRepository userRepository;

  //    @DeleteMapping("/register/nickname")
  //    public ResponseEntity<Void> delete(@RequestBody @Valid UserRequest userRequest) {
  //        User user = userService.findUserById(userRequest.getUserId());
  //
  //        userService.deleteUser(user.getId());
  //        return ResponseEntity.ok().build();
  //    }

  @Operation(summary = "회원 탈퇴", description = "서비스에서 탈퇴합니다")
  @DeleteMapping("/me")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@AuthenticationPrincipal User user) {
    userService.deleteUser(user.getId());
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "아바타 수정", description = "새로운 아바타 정보로 업데이트합니다")
  @PatchMapping("/me/{avatarId}")
  public ResponseEntity<ApiResponse<Void>> updateAvatar(
      @AuthenticationPrincipal User user,
      @RequestBody @Valid AvatarChangeRequest request,
      @RequestParam("avatarId") Long avatarId) {

    userService.updateAvatar(user, request, avatarId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "유저 닉네임 수정", description = "유저 닉네임을 수정합니다")
  @PatchMapping("/me/nickname")
  public ResponseEntity<ApiResponse<Void>> updateNickname(
      @AuthenticationPrincipal User user, @RequestBody @Valid NicknameChangeRequest request) {
    userService.updateNickname(user, request.getNewNickname());
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "내 정보 조회", description = "내 정보를 조회합니다")
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserRegisterResponse>> getMyInfo(
      @AuthenticationPrincipal User user) {
    // SecurityContextHolder에서 현재 인증된 사용자(UUID)를 가져옴
    UserRegisterResponse userRegisterResponse =
        new UserRegisterResponse(
            user.getId(), user.getNickname(), user.getUuid(), null, null); // 토큰은 응답에 포함하지 않음
    return ResponseEntity.ok(ApiResponse.success(userRegisterResponse));
  }

  @GetMapping("/level")
  @Operation(summary = "점수 및 레벨 조회")
  public ResponseEntity<ApiResponse<UserRegisterResponse.LevelStatusResponseDTO>> getLevel(
      @AuthenticationPrincipal User user) {
    UserRegisterResponse.LevelStatusResponseDTO levelStatusResponseDTO = userService.getLevel(user);
    return ResponseEntity.ok(ApiResponse.success(levelStatusResponseDTO));
  }

  @Operation(
      summary = "내 텃밭 ID 목록 조회",
      description = "현재 로그인한 유저와 연결된 모든 텃밭의 ID 목록을 조회합니다. 텃밭 슬롯 번호 순으로 정렬됩니다.")
  @GetMapping("/me/gardens")
  public ResponseEntity<ApiResponse<List<Long>>> getMyGardenIds(
      @AuthenticationPrincipal User user) {
    // 로직을 서비스 계층으로 위임하여 트랜잭션 내에서 처리하도록 변경
    List<Long> gardenIds = userService.getMyGardenIds(user);
    return ResponseEntity.ok(ApiResponse.success(gardenIds));
  }

  @Operation(summary = "유저 정보 조회", description = "유저 정보를 조회합니다")
  @GetMapping("/{userId}")
  public ResponseEntity<ApiResponse<UserProfileResponse>> getUserInfo(
      @PathParam("userId") Long userId) {
    // SecurityContextHolder에서 현재 인증된 사용자(UUID)를 가져옴
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다."));
    return ResponseEntity.ok(ApiResponse.success(userService.getUserProfile(user.getId(), userId)));
  }
}
