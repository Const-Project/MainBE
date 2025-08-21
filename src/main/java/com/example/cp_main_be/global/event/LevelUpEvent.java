package com.example.cp_main_be.global.event;

import com.example.cp_main_be.domain.member.user.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LevelUpEvent {
  private final User user;
  private final long achievedLevel; // 필요하다면 레벨업한 레벨 정보도 추가 가능
}
