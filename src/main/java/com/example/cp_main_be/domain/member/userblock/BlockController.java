package com.example.cp_main_be.domain.member.userblock;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/blocks")
@Tag(name = "사용자 차단 API")
public class BlockController {
  private final BlockService blockService;

  @PostMapping
  @Operation(summary = "사용자 숨기기/차단")
  public ResponseEntity<ApiResponse<Void>> blockUser(
      @AuthenticationPrincipal User user, @RequestBody BlockUserRequest request) {
    blockService.blockUser(user, request.getUserIdToBlock());
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @DeleteMapping
  @Operation(summary = "사용자 차단 해제")
  public ResponseEntity<ApiResponse<Void>> unblockUser(
      @AuthenticationPrincipal User user, @RequestBody BlockUserRequest request) {
    blockService.unblockUser(user, request.getUserIdToBlock());
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
