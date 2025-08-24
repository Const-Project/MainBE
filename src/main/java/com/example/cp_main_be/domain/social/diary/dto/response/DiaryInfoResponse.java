package com.example.cp_main_be.domain.social.diary.dto.response;

import com.example.cp_main_be.domain.social.comment.domain.Comment;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public record DiaryInfoResponse(
    Long id,
    Long writerId,
    String title,
    String content,
    String imageUrl,
    boolean isLiked,
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

    return new DiaryInfoResponse(
        diary.getId(),
        diary.getUser().getId(),
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
