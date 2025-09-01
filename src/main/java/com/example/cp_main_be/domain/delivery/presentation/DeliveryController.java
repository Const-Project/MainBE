package com.example.cp_main_be.domain.delivery.presentation;

import com.example.cp_main_be.domain.delivery.dto.request.DeliveryRequest;
import com.example.cp_main_be.domain.delivery.dto.response.DeliveryPlantResponse;
import com.example.cp_main_be.domain.delivery.dto.response.DeliveryResponse;
import com.example.cp_main_be.domain.delivery.service.DeliveryService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
@Tag(name = "씨앗 배송 api")
public class DeliveryController {

  private final DeliveryService deliveryService;

  @Operation(summary = "씨앗 배송 정보를 입력받습니다")
  @PostMapping("/seeds")
  public ResponseEntity<ApiResponse<Void>> requestDelivery(
      @AuthenticationPrincipal User user, // 현재 로그인한 사용자 정보
      @Valid @RequestBody DeliveryRequest deliveryRequest) {

    deliveryService.createDeliveryRequest(user.getId(), deliveryRequest);

    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "배송 가능한 식물 목록 조회")
  @GetMapping("/plants")
  public ResponseEntity<ApiResponse<List<DeliveryPlantResponse>>> getDeliveryPlantList() {
    List<DeliveryPlantResponse> deliveryPlantList = deliveryService.getDeliveryPlantList();
    return ResponseEntity.ok(ApiResponse.success(deliveryPlantList));
  }

  @Operation(summary = "배송 정보 조회", description = "지금까지 한 나의 모든 배송 정보를 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<List<DeliveryResponse>>> getMyDelivery(
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(ApiResponse.success(deliveryService.getMyDelivery(user)));
  }
}
