package com.example.cp_main_be.domain.member.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvatarChangeRequest {

  private String newAvatarUrl;
  private String newAvatarName;
}
