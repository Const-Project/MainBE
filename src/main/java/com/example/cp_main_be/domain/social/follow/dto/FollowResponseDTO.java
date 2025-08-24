package com.example.cp_main_be.domain.social.follow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowResponseDTO {
  private Long userId;
  private String username;
  private String userImageUrl;
}
