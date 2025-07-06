package com.example.cp_main_be.user.presentation;

import com.example.cp_main_be.user.domain.User;
import com.example.cp_main_be.user.dto.request.UserRequest;
import com.example.cp_main_be.user.dto.response.UserResponse;
import com.example.cp_main_be.user.service.UserService;
import com.example.cp_main_be.util.ApiResponse;
import jakarta.validation.Valid;
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

}
