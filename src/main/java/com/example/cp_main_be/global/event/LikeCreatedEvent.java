package com.example.cp_main_be.global.event;

import com.example.cp_main_be.domain.member.user.domain.User;
import lombok.Getter;

@Getter
public class LikeCreatedEvent {
  private final User owner; // 콘텐츠 소유자
  private final User writer; // 좋아요를 누른 사람
  private final Long targetId; // 콘텐츠 ID
  private final String targetType; // 콘텐츠 타입 ("DIARY", "AVATAR_POST" 등)

  public LikeCreatedEvent(User owner, User writer, Long targetId, String targetType) {
    this.owner = owner;
    this.writer = writer;
    this.targetId = targetId;
    this.targetType = targetType;
  }
}
