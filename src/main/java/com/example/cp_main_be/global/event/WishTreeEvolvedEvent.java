package com.example.cp_main_be.global.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class WishTreeEvolvedEvent {
  private final Long userId;
}
