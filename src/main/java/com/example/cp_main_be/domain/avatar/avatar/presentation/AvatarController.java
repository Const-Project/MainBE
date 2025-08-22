package com.example.cp_main_be.domain.avatar.avatar.presentation;

import com.example.cp_main_be.domain.avatar.avatar.domain.AvatarMaster;
import com.example.cp_main_be.domain.avatar.avatar.domain.repository.AvatarMasterRepository;
import com.example.cp_main_be.domain.avatar.avatar.dto.request.CreateAvatarRequest;
import com.example.cp_main_be.domain.avatar.avatar.dto.response.AvatarMasterResponse;
import com.example.cp_main_be.domain.avatar.avatar.service.AvatarService;
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
  private final AvatarMasterRepository avatarMasterRepository;

  @Operation(summary = "선택 가능한 아바타 종류 조회")
  @GetMapping("/masters") // 엔드포인트 변경
  public ResponseEntity<ApiResponse<List<AvatarMasterResponse>>> getSelectableAvatarMasters() {
    // AvatarMaster 목록을 조회
    List<AvatarMaster> masters = avatarMasterRepository.findAll();
    // DTO로 변환하여 반환
    List<AvatarMasterResponse> response = masters.stream().map(AvatarMasterResponse::from).toList();
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "내 아바타 생성")
  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createMyAvatar(
      @AuthenticationPrincipal User user, @RequestBody CreateAvatarRequest request) {
    avatarService.createAvatar(user.getId(), request.getMasterId(), request.getNickname());
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "꽃가루 주기", description = "남의 아바타에게 꽃가루를 줍니다")
  @PostMapping("/{avatarId}/pollen")
  public ResponseEntity<ApiResponse<Void>> givePollen(
      @AuthenticationPrincipal User user, @PathVariable Long avatarId) {
    avatarService.givePollen(user.getId(), avatarId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
