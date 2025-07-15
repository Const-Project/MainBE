package com.example.cp_main_be.user.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class UserAvatarRequest {

//    private final UUID userUuid;
    private final Long userId;
    private final Long avatarId;
}
