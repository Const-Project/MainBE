package com.example.cp_main_be.domain.social.avatarpost.dto;

import com.example.cp_main_be.domain.social.comment.domain.Comment;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostInfoResponse {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private boolean isLiked;
    private int likeCount;
    private int commentCount;
    private List<CommentResponseDTO> comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @JsonProperty("public")
    private boolean isPublic;
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommentResponseDTO {
      private Long commentId;
      private String profileImageUrl;
      private String writer;
      private String content;
    }
}
