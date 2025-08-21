package com.example.cp_main_be.domain.social.guestbook.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.domain.social.guestbook.dto.request.GuestbookRequest;
import com.example.cp_main_be.domain.social.guestbook.service.GuestbookService;
import com.example.cp_main_be.global.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "방명록 API", description = "방명록 관련 기능을 제공합니다.")
public class GuestbookController {

  private final GuestbookService guestbookService;
  private final UserService userService;

  @Operation(summary = "방명록 작성", description = "방명록을 다른 유저의 텃밭에 작성합니다")
  @PostMapping("/{userId}/guestbook")
  public ResponseEntity<ApiResponse<Void>> createGuestbook(
      @PathVariable Long userId, @RequestBody @Valid GuestbookRequest request) {
    String writerUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User writer = userService.findUserByUuid(UUID.fromString(writerUuid));
    guestbookService.createGuestbook(writer.getId(), userId, request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
