package com.example.cp_main_be.domain.social.feed.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.userblock.UserBlockRepository;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import com.example.cp_main_be.domain.social.diary.domain.Repository.DiaryRepository;
import com.example.cp_main_be.domain.social.feed.dto.response.FeedResponse;
import com.example.cp_main_be.domain.social.follow.domain.Follow;
import com.example.cp_main_be.domain.social.follow.domain.repository.FollowRepository;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

  private final UserRepository userRepository;
  private final DiaryRepository diaryRepository;
  private final FollowRepository followRepository;
  private final AvatarPostRepository avatarPostRepository;
  private final UserBlockRepository userBlockRepository;

  public List<FeedResponse> getFeed(UUID currentUserUuid, String filter, int page, int size) {
    // 올바른 페이지네이션을 위해, 각 소스에서 요청된 페이지의 끝까지 데이터를 충분히 가져옵니다.
    // 예: 2페이지(page=1) 20개(size=20) 요청 시, (1+1)*20=40개의 후보를 가져옵니다.
    // 이는 메모리 사용량과 성능에 영향을 줄 수 있으므로, 매우 깊은 페이지네이션에는 다른 전략(커서 기반)이 더 좋습니다.
    int limit = (page + 1) * size;
    Pageable candidatePageable =
        PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

    User currentUser =
        userRepository
            .findByUuid(currentUserUuid)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

    // [추가] 1. 현재 사용자가 차단한 유저 ID 목록을 먼저 조회합니다.
    List<Long> blockedUserIds = userBlockRepository.findBlockedUserIdsByBlocker(currentUser);

    Stream<FeedResponse> diaryStream;
    Stream<FeedResponse> avatarPostStream;

    if ("following".equalsIgnoreCase(filter)) {
      List<User> followingUsers =
          followRepository.findByFollower(currentUser).stream().map(Follow::getFollowing).toList();

      // [수정] 2. 차단된 유저를 제외하고 조회
      diaryStream =
          diaryRepository
              .findByUserInAndIsPublicIsTrueAndUser_IdNotIn(
                  followingUsers, blockedUserIds, candidatePageable)
              .stream()
              .map(FeedResponse::from);
      avatarPostStream =
          avatarPostRepository
              .findByUserInAndUser_IdNotIn(followingUsers, blockedUserIds, candidatePageable)
              .stream()
              .map(FeedResponse::from);

    } else {
      // [수정] 2. 차단된 유저를 제외하고 조회
      diaryStream =
          diaryRepository
              .findByIsPublicIsTrueAndUser_IdNotIn(blockedUserIds, candidatePageable)
              .stream()
              .map(FeedResponse::from);
      avatarPostStream =
          avatarPostRepository.findAllByUser_IdNotIn(blockedUserIds, candidatePageable).stream()
              .map(FeedResponse::from);
    }

    // 3. 두 스트림을 합치고, 전체 목록을 생성 시간 기준으로 다시 정렬합니다.
    List<FeedResponse> sortedFeed =
        Stream.concat(diaryStream, avatarPostStream)
            .sorted(Comparator.comparing(FeedResponse::createdAt).reversed())
            .toList();

    // 4. 정렬된 전체 목록에서 요청된 페이지에 해당하는 부분만 잘라내어 반환합니다.
    int start = page * size;
    if (start >= sortedFeed.size()) {
      return Collections.emptyList(); // 요청된 페이지가 데이터 범위를 벗어난 경우 빈 리스트 반환
    }
    int end = Math.min(start + size, sortedFeed.size());

    return sortedFeed.subList(start, end);
  }
}
