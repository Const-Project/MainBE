package com.example.cp_main_be.domain.social.feed.dto.response;

import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

/**
 * Diary, AvatarPost 등 다양한 타입의 게시물을 통합 피드에서 공통된 형식으로 표현하기 위한 DTO입니다. 이 하나의 클래스로 통합하여 관리의 용이성과 코드의
 * 일관성을 높입니다.
 */
public record FeedResponse(
    Long postId, PostType postType, String imageUrl, @JsonIgnore LocalDateTime createdAt) {

  public static FeedResponse from(Diary diary) {
    String imageUrl = diary.getDiaryImage() != null ? diary.getDiaryImage().getImageUrl() : null;
    return new FeedResponse(diary.getId(), PostType.DIARY, imageUrl, diary.getCreatedAt());
  }

  public static FeedResponse from(AvatarPost avatarPost) {
    // AvatarPost에 imageUrl 필드가 있다고 가정합니다. 필드명은 실제 코드에 맞게 수정해주세요.
    return new FeedResponse(
        avatarPost.getId(),
        PostType.AVATAR_POST,
        avatarPost.getImageUrl(),
        avatarPost.getCreatedAt());
  }
}
