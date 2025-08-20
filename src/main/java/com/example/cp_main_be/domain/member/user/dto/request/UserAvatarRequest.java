package com.example.cp_main_be.domain.member.user.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserAvatarRequest {

  //    private final UUID userUuid;
  private final Long userId;
  private final Long avatarId;
}
