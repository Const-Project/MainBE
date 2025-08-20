package com.example.cp_main_be.domain.content.avatar.presentation;

import com.example.cp_main_be.domain.content.avatar.dto.response.AvatarResponse;
import com.example.cp_main_be.domain.content.avatar.service.AvatarService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.global.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/avatars")
@RequiredArgsConstructor
@Tag(name = "아바타 API", description = "아바타 관련 기능을 제공합니다.")
public class AvatarController {

  private final AvatarService avatarService;

  @Operation(summary = "아바타 선택 목록 조회", description = "선택 가능한 아바타 목록을 반환합니다")
  @GetMapping("/register/avatars")
  public ResponseEntity<AvatarResponse> selectableAvatars() {
    AvatarResponse avatarResponse = new AvatarResponse(avatarService.getAllAvatar());
    return ResponseEntity.ok(avatarResponse);
  }

  @Operation(summary = "꽃가루 주기", description = "남의 아바타에게 꽃가루를 줍니다")
  @PostMapping("/{avatarId}/pollen")
  public ResponseEntity<ApiResponse<Void>> givePollen(
      @AuthenticationPrincipal User user, @PathVariable Long avatarId) {
    avatarService.givePollen(user.getId(), avatarId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
