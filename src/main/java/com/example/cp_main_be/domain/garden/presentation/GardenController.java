package com.example.cp_main_be.domain.garden.presentation;

import com.example.cp_main_be.domain.garden.dto.GardenResponse;
import com.example.cp_main_be.domain.garden.service.GardenService;
import com.example.cp_main_be.global.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/gardens")
public class GardenController {

  private final GardenService gardenService;

  @GetMapping("/{gardenId}")
  public ResponseEntity<ApiResponse<GardenResponse>> getGarden(@PathVariable Long gardenId) {
    GardenResponse gardenResponse = gardenService.findGardenById(gardenId);
    return ResponseEntity.ok(ApiResponse.success(gardenResponse));
  }
}
