package com.example.cp_main_be.domain.avatar.avatar.presentation;

import com.example.cp_main_be.domain.avatar.avatar.domain.AvatarMaster;
import com.example.cp_main_be.domain.avatar.avatar.domain.repository.AvatarMasterRepository;
import com.example.cp_main_be.domain.avatar.avatar.dto.request.CreateAvatarRequest;
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

  public record FallbackImageResponse(String imageUrl) {}

  @Operation(summary = "내 아바타 생성")
  @PostMapping
  public ResponseEntity<ApiResponse<?>> createMyAvatar(
      @AuthenticationPrincipal User user, @RequestBody CreateAvatarRequest request) {
    try {
      // 2. [성공 로직] 기존과 동일하게 서비스 호출
      avatarService.createAvatar(user.getId(), request.getMasterId(), request.getNickname());

      // 성공 시, 데이터가 없는 성공 응답 반환
      return ResponseEntity.ok(ApiResponse.success(null));

    } catch (CustomApiException e) {
      // 3. [실패 로직] 예외를 catch하여 폴백 처리
      log.warn("아바타 생성 실패. 기본 이미지 URL을 반환합니다. 원인: {}", e.getMessage());

      // ImageProcessingService에서 랜덤 URL 가져오기
      String fallbackImageUrl = imageProcessingService.getDefaultImageUrl();

      // 가져온 URL을 응답 DTO에 담기
      FallbackImageResponse responseDto = new FallbackImageResponse(fallbackImageUrl);

      // DTO를 ApiResponse로 감싸서 성공 응답으로 반환 (HTTP 요청 자체는 성공했으므로)
      return ResponseEntity.ok(ApiResponse.success(responseDto));
    }
  }

  @Operation(summary = "꽃가루 주기", description = "남의 아바타에게 꽃가루를 줍니다")
  @PostMapping("/{avatarId}/pollen")
  public ResponseEntity<ApiResponse<Void>> givePollen(
      @AuthenticationPrincipal User user, @PathVariable Long avatarId) {
    avatarService.givePollen(user.getId(), avatarId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
