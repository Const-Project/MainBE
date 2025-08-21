package com.example.cp_main_be.global.event;

import com.example.cp_main_be.domain.social.guestbook.domain.Guestbook;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GuestbookCreatedEvent {
  private final Guestbook guestbook;
}
