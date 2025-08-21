package com.example.cp_main_be.domain.garden.garden.presentation;

import com.example.cp_main_be.domain.garden.garden.dto.GardenResponse;
import com.example.cp_main_be.domain.garden.garden.service.GardenService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/gardens")
@Tag(name = "텃밭 API", description = "텃밭 관련 기능을 제공합니다.")
public class GardenController {

  private final GardenService gardenService;

  @Operation(summary = "텃밭 슬롯 조회", description = "텃밭 슬롯을 조회합니다")
  @GetMapping("/{gardenId}")
  public ResponseEntity<ApiResponse<GardenResponse>> getGarden(@PathVariable Long gardenId) {
    GardenResponse gardenResponse = gardenService.findGardenById(gardenId);
    return ResponseEntity.ok(ApiResponse.success(gardenResponse));
  }

  @Operation(summary = "정원에 물 주기", description = "자신 또는 다른 사람의 정원에 물을 줍니다.")
  @PostMapping("/{gardenId}/water")
  public ResponseEntity<ApiResponse<Void>> waterGarden(
      @AuthenticationPrincipal Long userId, @PathVariable Long gardenId) {
    gardenService.waterGarden(userId, gardenId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "정원에 햇빛 주기", description = "자신의 정원에 햇빛을 줍니다.")
  @PostMapping("/{gardenId}/sunlight")
  public ResponseEntity<ApiResponse<Void>> sunlightGarden(
      @AuthenticationPrincipal Long userId, @PathVariable Long gardenId) {
    gardenService.sunlightGarden(userId, gardenId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
