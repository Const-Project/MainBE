package com.example.cp_main_be.domain.social.avatarpost.dto;

import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.comment.domain.Comment;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public record PostInfoResponse(
    Long id,
    Long writerId,
    String writerName,
    String profileImageUrl,
    String content,
    String imageUrl,
    boolean isLiked,
    long likeCount,
    int commentCount,
    List<CommentResponseDTO> comments,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    @JsonProperty("isPublic") boolean isPublic) {

  public static PostInfoResponse from(
      AvatarPost post, boolean isLiked, long likeCount, List<Comment> comments) {
    List<CommentResponseDTO> commentDTOs = comments.stream().map(CommentResponseDTO::from).toList();
    String writerName = post.getUser().getNickname();
    String profileImageUrl = post.getUser().getProfileImageUrl();
    return new PostInfoResponse(
        post.getId(),
        post.getUser().getId(),
        writerName,
        profileImageUrl,
        post.getCaption(),
        post.getImageUrl(),
        isLiked,
        likeCount,
        commentDTOs.size(),
        commentDTOs,
        post.getCreatedAt(),
        post.getUpdatedAt(),
        true // AvatarPost는 항상 public이라고 가정
        );
  }

  public record CommentResponseDTO(
      Long commentId, Long writerId, String profileImageUrl, String writer, String content) {
    public static CommentResponseDTO from(Comment comment) {
      return new CommentResponseDTO(
          comment.getId(),
          comment.getWriter().getId(),
          comment.getWriter().getProfileImageUrl(),
          comment.getWriter().getNickname(),
          comment.getContent());
    }
  }
}
