package com.example.cp_main_be.domain.avatar.dto.response;

import com.example.cp_main_be.domain.avatar.domain.Avatar;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AvatarResponse {

  List<Avatar> avatars = new ArrayList<>();
}
