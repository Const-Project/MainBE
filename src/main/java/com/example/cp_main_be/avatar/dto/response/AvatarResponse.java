package com.example.cp_main_be.avatar.dto.response;

import com.example.cp_main_be.avatar.domain.Avatar;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;



@Getter
@RequiredArgsConstructor
public class AvatarResponse {

    private final List<Avatar> avatars;
}
