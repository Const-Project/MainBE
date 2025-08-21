package com.example.cp_main_be.global.event;

import com.example.cp_main_be.domain.member.user.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class WateredByFriendEvent {
  private final User receiver;
  private final User sender;
}
