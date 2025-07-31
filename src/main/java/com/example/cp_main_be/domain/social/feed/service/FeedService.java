package com.example.cp_main_be.domain.social.feed.service;

import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

  private final UserRepository userRepository;

  public List<Object> getFeed(UUID userUuid, String filter) {
    User currentUser =
        userRepository
            .findByUuid(userUuid)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

    List<Object> feedItems = new ArrayList<>();

    // TODO: 일기 및 아바타 포스트 데이터를 가져와서 통합하는 로직 구현
    // filter 파라미터에 따라 팔로우한 사용자의 게시물만 필터링
    if ("following".equalsIgnoreCase(filter)) {
      // 팔로우한 사용자의 게시물만 가져오는 로직
    } else {
      // 모든 사용자의 게시물을 가져오는 로직
    }

    return feedItems;
  }
}
