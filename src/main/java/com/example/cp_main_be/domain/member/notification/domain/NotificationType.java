package com.example.cp_main_be.domain.member.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
  FOLLOW("follow", "%s님이 회원님을 팔로우하기 시작했습니다.", false),
  POLLEN("pollen", "%s님이 회원님의 식물에 꽃가루를 주었습니다.", false),
  FEED_LIKE("feed_like", "%s님이 회원님의 일기를 좋아합니다.", false),
  DIARY_LIKE("diary_like", "%s님이 회원님의 일기를 좋아합니다.", false),
  AVATAR_POST_LIKE("avatar_post_like", "%s님이 회원님의 포스트를 좋아합니다.", false),
  FEED_COMMENT("feed_comment", "%s님이 회원님의 일기에 댓글을 남겼습니다.", false),
  GUESTBOOK("guestbook", "%s님이 방명록에 글을 남겼습니다.", false),
  WATERING("watering", "%s에게 물 줄 시간이에요!", false),
  SUNSHINE("sunshine", "식물에게 햇빛을 줄 시간이에요!", false),
  DIARY_COMMENT("diary_comment", "내 일기에 누군가 댓글을 달았습니다.", false),
  AVATAR_POST_COMMENT("avatar_comment", "내 아바타 포스트에 누군가 댓글을 달았습니다.", false),
  WATERING_BY_FRIEND("watering_by_friend", "누군가 내 아바타에게 물을 주었습니다.", false),
  POLLEN_AVAILABLE("pollen_available", "꽃가루가 쌓여있어요, 친구들에게 나눠봐요!", false),
  REPORT_PROCESSED("report_processed", "회원님의 신고가 처리되었습니다.", false),
  REPORT_RECEIVED("report_received", "누군가 회원님을 신고했습니다. 뻐꾸기가 지켜보고 있어요!", false),
  AVATAR_CREATED("avatar_created", "새 아바타가 만들어졌어요!", false),
  SEED_DELIVERY("seed_delivery", "씨앗 배송이 시작되었어요!", false);

  private final String type;
  private final String messageTemplate;
  private final boolean marketing;
}
