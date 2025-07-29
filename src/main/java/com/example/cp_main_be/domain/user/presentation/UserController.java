package com.example.cp_main_be.domain.user.presentation;

import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.dto.request.UserRequest;
import com.example.cp_main_be.domain.user.dto.response.UserResponse;
import com.example.cp_main_be.domain.user.service.UserService;
import com.example.cp_main_be.global.util.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

  private final UserService userService;

  /** 닉네임으로 유저등록하기 */
  @PostMapping("/register/nickname")
  public ResponseEntity<ApiResponse<UserResponse>> register(
      @RequestBody @Valid UserRequest userRequest) {
    User user = new User();
    user.setUsername(userRequest.getUsername());
    userService.saveUser(user);

    UserResponse userResponse = new UserResponse(user.getId(), user.getUsername(), user.getUuid());
    return ResponseEntity.ok(ApiResponse.success(userResponse));
  }

  //    @DeleteMapping("/register/nickname")
  //    public ResponseEntity<Void> delete(@RequestBody @Valid UserRequest userRequest) {
  //        User user = userService.findUserById(userRequest.getUserId());
  //
  //        userService.deleteUser(user.getId());
  //        return ResponseEntity.ok().build();
  //    }

  @GetMapping("/users/{uuid}")
  public ResponseEntity<ApiResponse<UserResponse>> getUserInfo(@PathVariable UUID uuid) {
    User user = userService.findUserByUuid(uuid);
    UserResponse userResponse = new UserResponse(user.getId(), user.getUsername(), user.getUuid());
    return ResponseEntity.ok(ApiResponse.success(userResponse));
  }
}
