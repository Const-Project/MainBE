package com.example.cp_main_be.domain.social.guestbook.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

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
