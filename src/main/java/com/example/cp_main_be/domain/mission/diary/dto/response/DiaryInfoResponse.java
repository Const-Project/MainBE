package com.example.cp_main_be.domain.mission.diary.dto.response;

import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import com.example.cp_main_be.domain.social.comment.domain.Comment;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public record DiaryInfoResponse(
    Long id,
    Long writerId,
    String writerName,
    String profileImageUrl,
    String title,
    String content,
    String imageUrl,
    boolean isLiked,
    long likeCount, // [수정] int -> long
    int commentCount,
    List<CommentResponseDTO> comments,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    @JsonProperty("isPublic") boolean isPublic) {

  // [수정] 생성자에서 likeCount를 직접 받도록 변경
  public static DiaryInfoResponse from(
      Diary diary, boolean isLiked, long likeCount, List<Comment> comments) {
    List<CommentResponseDTO> commentDTOs = comments.stream().map(CommentResponseDTO::from).toList();
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
        likeCount, // [수정] 파라미터로 받은 값을 사용
        commentDTOs.size(),
        commentDTOs,
        diary.getCreatedAt(),
        diary.getUpdatedAt(),
        diary.isPublic());
  }

  public record CommentResponseDTO(
      Long commentId, Long writerId, String profileImageUrl, String writer, String content) {
    public static CommentResponseDTO from(Comment comment) {
      String profileImageUrl =
          comment.getWriter() != null ? comment.getWriter().getProfileImageUrl() : null;
      return new CommentResponseDTO(
          comment.getId(),
          comment.getWriter() != null ? comment.getWriter().getId() : null,
          profileImageUrl,
          comment.getWriter().getNickname(),
          comment.getContent());
    }
  }
}
