package com.example.cp_main_be.user.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserRequest {

    private final Long userUuid;
    private final Long userId;
    private final String username;
}
