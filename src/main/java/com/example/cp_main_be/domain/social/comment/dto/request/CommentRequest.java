package com.example.cp_main_be.domain.social.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {
  @NotBlank(message = "댓글 내용은 필수입니다.")
  private String content;

  @NotNull(message = "대상 ID는 필수입니다.")
  private Long targetId;

  @NotBlank(message = "대상 타입은 필수입니다.")
  private String targetType;
}
