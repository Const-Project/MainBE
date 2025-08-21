package com.example.cp_main_be.domain.garden.garden.presentation;

import com.example.cp_main_be.domain.garden.garden.dto.GardenResponse;
import com.example.cp_main_be.domain.garden.garden.dto.response.GardenBackgroundCandidateResponse;
import com.example.cp_main_be.domain.garden.garden.dto.response.GardenBackgroundResponse;
import com.example.cp_main_be.domain.garden.garden.service.GardenService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.global.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/gardens")
public class GardenController {

  private final GardenService gardenService;
  private final UserService userService;

  @Operation(summary = "텃밭 가져오기", description = "텃밭 가져오기")
  @GetMapping("/{gardenId}")
  public ResponseEntity<ApiResponse<GardenResponse>> getGarden(@PathVariable Long gardenId) {
    GardenResponse gardenResponse = gardenService.findGardenById(gardenId);
    return ResponseEntity.ok(ApiResponse.success(gardenResponse));
  }

  @Operation(summary = "텃밭 물주기", description = "텃밭 waterCount 증가")
  @PutMapping("/{gardenId}/water")
  public ResponseEntity<ApiResponse<Void>> waterGarden(@PathVariable Long gardenId) {
    gardenService.waterGarden(gardenId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "텃밭 햇빛주기", description = "텃밭 sunlightCount 증가")
  @PutMapping({"/{gardenId}/sunlight"})
  public ResponseEntity<ApiResponse<Void>> sunlightGarden(@PathVariable Long gardenId) {
    gardenService.sunlightGarden(gardenId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "텃밭 슬롯 확장", description = "텃밭 슬롯 확장")
  @PutMapping("/slots/unlock")
  public ResponseEntity<ApiResponse<Void>> unlockGarden() {
    User currentUser = userService.getCurrentUser();
    gardenService.unlockGarden(currentUser);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "텃밭 배경화면 get", description = "텃밭의 배경화면을 조회한다.")
  @GetMapping("/{gardenId}/background")
  public ResponseEntity<ApiResponse<GardenBackgroundResponse>> getBackgroundImage(
      @PathVariable Long gardenId) {
    GardenBackgroundResponse response = gardenService.getGardenBackgroundImage(gardenId);
    return ResponseEntity.ok(ApiResponse.success(response));
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
