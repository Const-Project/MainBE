package com.example.cp_main_be.global.event;

import com.example.cp_main_be.domain.social.follow.domain.Follow;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FollowedEvent {
  private final Follow follow;
}
