package com.example.cp_main_be.domain.avatar.avatar.dto.request;

import lombok.Getter;

@Getter
public class CreateAvatarRequest {
  private Long masterId;
  private String nickname;
}
