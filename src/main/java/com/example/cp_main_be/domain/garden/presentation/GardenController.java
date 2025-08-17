package com.example.cp_main_be.domain.garden.presentation;

import com.example.cp_main_be.domain.garden.dto.GardenResponse;
import com.example.cp_main_be.domain.garden.service.GardenService;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.service.UserService;
import com.example.cp_main_be.global.util.ApiResponse;
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

  //  @GetMapping("/background/{backgroundId}")
  //  public ResponseEntity<ApiResponse<BackgroundResponse>> getBackground(
  //          @PathVariable Long backgroundId) {
  //
  //    BackgroundResponse backgroundResponse = gardenService.getBackgroundById(backgroundId);
  //
  //    return ResponseEntity.ok(ApiResponse.success(backgroundResponse));
  //  }

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
}
