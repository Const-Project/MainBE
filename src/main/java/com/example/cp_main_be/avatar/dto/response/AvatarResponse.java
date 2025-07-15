package com.example.cp_main_be.avatar.dto.response;

import com.example.cp_main_be.avatar.domain.Avatar;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AvatarResponse {

  private final List<Avatar> avatars;
}
