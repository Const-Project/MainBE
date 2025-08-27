package com.example.cp_main_be.domain.garden.garden.presentation;

import com.example.cp_main_be.domain.garden.garden.dto.response.GardenBackgroundCandidateResponse;
import com.example.cp_main_be.domain.garden.garden.dto.response.GardenResponse;
import com.example.cp_main_be.domain.garden.garden.service.GardenService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/gardens")
@Tag(name = "텃밭 API", description = "텃밭 관련 기능을 제공합니다.")
public class GardenController {

  private final GardenService gardenService;
  private final UserService userService;

  @Operation(summary = "텃밭 슬롯 조회", description = "텃밭 슬롯을 조회합니다")
  @GetMapping("/{gardenId}")
  public ResponseEntity<ApiResponse<GardenResponse>> getGarden(@PathVariable Long gardenId) {
    GardenResponse gardenResponse = gardenService.findGardenById(gardenId);
    return ResponseEntity.ok(ApiResponse.success(gardenResponse));
  }

  @Operation(summary = "내 정원에 물 주기", description = "자신의 정원에 물을 줍니다.")
  @PostMapping("/{gardenId}/mywater")
  public ResponseEntity<ApiResponse<Void>> waterMyGarden(
      @AuthenticationPrincipal User user, @PathVariable Long gardenId) {
    gardenService.waterGarden(user.getId(), gardenId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "남의 정원에 물 주기", description = "남의 정원에 물을 줍니다.")
  @PostMapping("/{gardenId}/friendwater")
  public ResponseEntity<ApiResponse<Void>> waterYourGarden(
      @AuthenticationPrincipal User user, @PathVariable Long gardenId) {
    gardenService.waterGarden(user.getId(), gardenId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "정원에 햇빛 주기", description = "자신의 정원에 햇빛을 줍니다.")
  @PostMapping("/{gardenId}/sunlight")
  public ResponseEntity<ApiResponse<Void>> sunlightGarden(
      @AuthenticationPrincipal User user, @PathVariable Long gardenId) {
    gardenService.sunlightGarden(user.getId(), gardenId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "텃밭 슬롯 확장", description = "텃밭 슬롯 확장")
  @PutMapping("/slots/unlock")
  public ResponseEntity<ApiResponse<Void>> unlockGarden() {
    User currentUser = userService.getCurrentUser();
    gardenService.unlockNewGardenSlot(currentUser.getId());
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "텃밭 배경화면 update", description = "텃밭의 배경화면을 수정한다.")
  @PutMapping("/{gardenId}/background/{backgroundId}")
  public ResponseEntity<ApiResponse<Void>> updateBackgroundImage(
      @PathVariable Long gardenId, @PathVariable Long backgroundId) {
    gardenService.updateGardenBackgroundImage(gardenId, backgroundId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "텃밭 배경화면 후보 조회", description = "텃밭 배경화면의 후보를 모두 조회한다.")
  @GetMapping("/backgrounds")
  public ResponseEntity<ApiResponse<List<GardenBackgroundCandidateResponse>>> getAllBackgrounds() {
    List<GardenBackgroundCandidateResponse> response = gardenService.getAllBackgrounds();
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
