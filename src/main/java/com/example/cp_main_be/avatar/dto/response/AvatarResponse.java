package com.example.cp_main_be.avatar.dto.response;

import com.example.cp_main_be.avatar.domain.Avatar;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;



@AllArgsConstructor
public class AvatarResponse {


    List<Avatar> avatars = new ArrayList<>();
}
