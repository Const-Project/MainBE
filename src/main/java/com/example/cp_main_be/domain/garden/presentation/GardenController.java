package com.example.cp_main_be.domain.garden.presentation;

import com.example.cp_main_be.domain.garden.dto.response.GardenBackgroundCandidateResponse;
import com.example.cp_main_be.domain.garden.dto.response.GardenBackgroundResponse;
import com.example.cp_main_be.domain.garden.dto.response.GardenResponse;
import com.example.cp_main_be.domain.garden.service.GardenService;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.service.UserService;
import com.example.cp_main_be.global.util.ApiResponse;
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

  @GetMapping("/{gardenId}")
  public ResponseEntity<ApiResponse<GardenResponse>> getGarden(@PathVariable Long gardenId) {
    GardenResponse gardenResponse = gardenService.findGardenById(gardenId);
    return ResponseEntity.ok(ApiResponse.success(gardenResponse));
  }

  @PutMapping("/{gardenId}/water")
  public ResponseEntity<ApiResponse<Void>> waterGarden(@PathVariable Long gardenId) {
    gardenService.waterGarden(gardenId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PutMapping({"/{gardenId}/sunlight"})
  public ResponseEntity<ApiResponse<Void>> sunlightGarden(@PathVariable Long gardenId) {
    gardenService.sunlightGarden(gardenId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PutMapping("/slots/unlock")
  public ResponseEntity<ApiResponse<Void>> unlockGarden() {
    User currentUser = userService.getCurrentUser();
    gardenService.unlockGarden(currentUser);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @GetMapping("/{gardenId}/background")
  public ResponseEntity<ApiResponse<GardenBackgroundResponse>> getBackgroundImage(
      @PathVariable Long gardenId) {
    GardenBackgroundResponse response = gardenService.getGardenBackgroundImage(gardenId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PutMapping("/{gardenId}/background/{backgroundId}")
  public ResponseEntity<ApiResponse<Void>> updateBackgroundImage(
      @PathVariable Long gardenId, @PathVariable Long backgroundId) {
    gardenService.updateGardenBackgroundImage(gardenId, backgroundId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @GetMapping("/backgrounds")
  public ResponseEntity<ApiResponse<List<GardenBackgroundCandidateResponse>>> getAllBackgrounds() {
    List<GardenBackgroundCandidateResponse> response = gardenService.getAllBackgrounds();
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
