package com.example.cp_main_be.user.dto.response;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserResponse {

  private final Long userId;
  private final String username;
  private final UUID uuid;
}
