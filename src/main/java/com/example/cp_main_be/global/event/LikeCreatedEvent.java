package com.example.cp_main_be.global.event;

import com.example.cp_main_be.domain.social.like.domain.Like;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LikeCreatedEvent {
  private final Like like;
}
