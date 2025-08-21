package com.example.cp_main_be.domain.social.guestbook.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GuestbookRequest {
  @NotBlank(message = "방명록 내용은 필수입니다.")
  public String content;
}
