package com.example.cp_main_be.global.dto;

import com.example.cp_main_be.domain.member.user.domain.User;
import lombok.Getter;

@Getter
public class AuthorResponse {
  private Long userId;
  private String username;
  private String profileImageUrl;

  public AuthorResponse(User user) {
    this.userId = user.getId();
    this.username = user.getUsername();
    this.profileImageUrl = user.getProfileImageUrl();
  }
}
