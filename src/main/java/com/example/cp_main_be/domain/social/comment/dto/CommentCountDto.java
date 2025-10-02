package com.example.cp_main_be.domain.social.comment.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CommentCountDto {
  // Setters (필요한 경우)
  // Getters
  private Long targetId;
  private Long count;

  // JPA 쿼리에서 사용할 생성자
  public CommentCountDto(Long targetId, Long count) {
    this.targetId = targetId;
    this.count = count;
  }
}
