package com.example.cp_main_be.domain.mission.diary.dto.response;

import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import com.example.cp_main_be.domain.social.comment.domain.Comment;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public record DiaryInfoResponse(
    Long id, // 일기 id
    Long writerId, // 작성자 id
    String writerName, // 작성자 이름
    String profileImageUrl, // 작성자 프로필 이미지 url
    String title, // 일기 제목
    String content, // 일기 내용
    String imageUrl, // 일기 이미지
    boolean isLiked, //
    int likeCount,
    int commentCount,
    List<CommentResponseDTO> comments,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    @JsonProperty("isPublic") boolean isPublic) {

  public static DiaryInfoResponse from(Diary diary, boolean isLiked) {
    List<CommentResponseDTO> commentDTOs =
        diary.getComments().stream().map(CommentResponseDTO::from).toList();
    String imageUrl = diary.getDiaryImage() != null ? diary.getDiaryImage().getImageUrl() : null;
    String writerName = diary.getUser().getNickname();
    String profileImageUrl = diary.getUser().getProfileImageUrl();

    return new DiaryInfoResponse(
        diary.getId(),
        diary.getUser().getId(),
        writerName,
        profileImageUrl,
        diary.getTitle(),
        diary.getContent(),
        imageUrl,
        isLiked,
        diary.getLikeCount(),
        commentDTOs.size(),
        commentDTOs,
        diary.getCreatedAt(),
        diary.getUpdatedAt(),
        diary.isPublic());
  }

  public record CommentResponseDTO(
      Long commentId, String profileImageUrl, String writer, String content) {
    public static CommentResponseDTO from(Comment comment) {
      return new CommentResponseDTO(
          comment.getId(),
          comment.getWriter().getProfileImageUrl(),
          comment.getWriter().getNickname(),
          comment.getContent());
    }
  }
}
