package com.example.cp_main_be.domain.social.guestbook.presentation;

import com.example.cp_main_be.domain.social.guestbook.dto.request.GuestbookRequest;
import com.example.cp_main_be.domain.social.guestbook.service.GuestbookService;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.service.UserService;
import com.example.cp_main_be.global.util.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class GuestbookController {

  private final GuestbookService guestbookService;
  private final UserService userService;

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
