package com.example.cp_main_be.domain.avatar.presentation;

import com.example.cp_main_be.domain.avatar.dto.response.AvatarResponse;
import com.example.cp_main_be.domain.avatar.service.AvatarService;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.global.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/avatars")
@RequiredArgsConstructor
public class AvatarController {

  private final AvatarService avatarService;

  @GetMapping("/register/avatars")
  public ResponseEntity<AvatarResponse> selectableAvatars() {
    AvatarResponse avatarResponse = new AvatarResponse(avatarService.getAllAvatar());
    return ResponseEntity.ok(avatarResponse);
  }

  @PostMapping("/{avatarId}/pollen")
  public ResponseEntity<ApiResponse<Void>> givePollen(
      @AuthenticationPrincipal User user, @PathVariable Long avatarId) {
    avatarService.givePollen(user.getId(), avatarId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
