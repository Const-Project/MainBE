package com.example.cp_main_be.domain.member.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

/** 현재 사용자와 프로필 사용자 간의 팔로우 관계를 나타냅니다. */
@JsonFormat(shape = JsonFormat.Shape.STRING) // JSON으로 변환 시 Enum 이름을 문자열로 사용
public enum FollowStatus {
  /** 팔로우하지 않는 상태 (친구 추가 버튼) */
  NOT_FOLLOWING,

  /** 내가 상대방을 팔로우하는 상태 (팔로우 취소 버튼) */
  FOLLOWING,

  /** 상대방이 나를 팔로우하는 상태 (맞팔로우 버튼) */
  FOLLOW_BACK_POSSIBLE
}
