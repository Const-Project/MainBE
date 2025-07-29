package com.example.cp_main_be.domain.user.dto.request;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserRequest {

  private final UUID userUuid;
  private final Long userId;
  private final String username;
}
