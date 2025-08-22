package com.example.cp_main_be.domain.avatar.avatar.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvatarResponse {
  List<AvatarSimpleResponse> avatars;
}
