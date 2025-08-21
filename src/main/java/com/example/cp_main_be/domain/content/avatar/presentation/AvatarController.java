package com.example.cp_main_be.domain.content.avatar.presentation;

import com.example.cp_main_be.domain.content.avatar.domain.Avatar;
import com.example.cp_main_be.domain.content.avatar.dto.response.AvatarResponse;
import com.example.cp_main_be.domain.content.avatar.dto.response.AvatarSimpleResponse;
import com.example.cp_main_be.domain.content.avatar.service.AvatarService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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

  @Operation(summary = "선택 가능 아바타들 조회", description = "선택할 아바타 목록을 조회")
  @GetMapping("/register/avatars")
  public ResponseEntity<ApiResponse<AvatarResponse>> getSelectableAvatars() {
    // 1. 서비스로부터 Avatar 엔티티 리스트를 받습니다.
    List<Avatar> avatarEntities = avatarService.getAllAvatar();

    // 2. 엔티티 리스트를 DTO 리스트로 변환합니다.
    List<AvatarSimpleResponse> avatarDtos =
        avatarEntities.stream().map(AvatarSimpleResponse::new).toList();

    // 3. DTO 리스트를 최종 응답 객체에 담아 반환합니다.
    AvatarResponse avatarResponse = new AvatarResponse(avatarDtos);
    return ResponseEntity.ok(ApiResponse.success(avatarResponse));
  }

  @Operation(summary = "꽃가루 주기", description = "남의 아바타에게 꽃가루를 줍니다")
  @PostMapping("/{avatarId}/pollen")
  public ResponseEntity<ApiResponse<Void>> givePollen(
      @AuthenticationPrincipal User user, @PathVariable Long avatarId) {
    avatarService.givePollen(user.getId(), avatarId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
