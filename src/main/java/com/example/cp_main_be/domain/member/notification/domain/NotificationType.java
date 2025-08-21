package com.example.cp_main_be.domain.member.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
  FOLLOW("follow", "%s님이 회원님을 팔로우하기 시작했습니다."),
  POLLEN("pollen", "%s님이 회원님의 식물에 꽃가루를 주었습니다."),
  FEED_LIKE("feed_like", "%s님이 회원님의 일기를 좋아합니다."),
  FEED_COMMENT("feed_comment", "%s님이 회원님의 일기에 댓글을 남겼습니다."),
  GUESTBOOK("guestbook", "%s님이 방명록에 글을 남겼습니다."),
  WATERING("watering", "%s에게 물 줄 시간이에요!"),
  SUNSHINE("sunshine", "식물에게 햇빛을 줄 시간이에요!"),
  DIARY_COMMENT("diary_comment", "내 일기에 누군가 댓글을 달았습니다."),
  AVATAR_POST_COMMENT("avatar_comment", "내 아바타 포스트에 누군가 댓글을 달았습니다."),
  POLLEN_AVAILABLE("pollen_available", "꽃가루가 쌓여있어요, 친구들에게 나눠봐요!");

  private final String type;
  private final String messageTemplate;
}
