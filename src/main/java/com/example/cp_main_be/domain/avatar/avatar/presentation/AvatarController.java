package com.example.cp_main_be.domain.avatar.avatar.presentation;

import com.example.cp_main_be.domain.avatar.avatar.domain.AvatarMaster;
import com.example.cp_main_be.domain.avatar.avatar.domain.repository.AvatarMasterRepository;
import com.example.cp_main_be.domain.avatar.avatar.dto.response.AvatarMasterResponse;
import com.example.cp_main_be.domain.avatar.avatar.service.AvatarService;
import com.example.cp_main_be.domain.avatar.image.service.ImageProcessingService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.global.common.ApiResponse;
import com.example.cp_main_be.global.common.CustomApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/avatars")
@RequiredArgsConstructor
@Tag(name = "아바타 API", description = "아바타 관련 기능을 제공합니다.")
public class AvatarController {

  private final AvatarService avatarService;
  private final AvatarMasterRepository avatarMasterRepository;
  private final ImageProcessingService imageProcessingService;

  @Operation(summary = "선택 가능한 아바타 종류 조회")
  @GetMapping("/masters") // 엔드포인트 변경
  public ResponseEntity<ApiResponse<List<AvatarMasterResponse>>> getSelectableAvatarMasters() {
    // AvatarMaster 목록을 조회
    List<AvatarMaster> masters = avatarMasterRepository.findAll();
    // DTO로 변환하여 반환
    List<AvatarMasterResponse> response = masters.stream().map(AvatarMasterResponse::from).toList();
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  // [수정] 최종 아바타 등록을 위한 통합 DTO
  // AI 생성 시 masterId는 null, 기존 선택 시 masterId에 해당 ID를 담아 요청
  public record CreateAvatarFinalRequest(String nickname, String imageUrl, Long masterId) {}

  @Operation(summary = "아바타 최종 등록", description = "AI로 생성했거나 기존 목록에서 선택한 아바타를 최종 등록합니다.")
  @PostMapping // [수정] 엔드포인트를 /api/v1/avatars 로 단순화
  public ResponseEntity<ApiResponse<Void>> createAvatar(
      @AuthenticationPrincipal User user, @RequestBody CreateAvatarFinalRequest request) {

    // [수정] 통합된 서비스 메서드 호출
    avatarService.createAvatar(
        user.getId(), request.nickname(), request.imageUrl(), request.masterId());

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
