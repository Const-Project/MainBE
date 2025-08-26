package com.example.cp_main_be.domain.social.guestbook.dto.response;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GuestbookResponse {

  public String author;
  public String content;
  public LocalDateTime createdAt;
}
