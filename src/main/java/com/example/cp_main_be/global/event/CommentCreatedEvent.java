package com.example.cp_main_be.global.event;

import com.example.cp_main_be.domain.social.comment.domain.Comment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommentCreatedEvent {
  private final Comment comment;
}
