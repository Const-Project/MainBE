package com.example.cp_main_be.domain.social.comment.dto.response;

import com.example.cp_main_be.domain.social.comment.domain.Comment;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentResponse {
  private Long id;
  private Long writerId;
  private String writer;
  private String content;
  private Long targetId;
  private String targetType;
  private LocalDateTime createAt;

  public static CommentResponse from(Comment comment, Long targetId, String targetType) {
    CommentResponse response = new CommentResponse();
    response.setId(comment.getId());
    response.setWriterId(comment.getWriter().getId());
    response.setWriter(comment.getWriter().getNickname());
    response.setContent(comment.getContent());
    response.setTargetId(targetId);
    response.setTargetType(targetType);
    response.setCreateAt(comment.getCreatedAt());
    return response;
  }
}
