package com.example.cp_main_be.domain.social.feed.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.userblock.UserBlockRepository;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import com.example.cp_main_be.domain.social.avatarpost.dto.AvatarPostFeedItemResponse;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.domain.Repository.DiaryRepository;
import com.example.cp_main_be.domain.social.diary.dto.response.DiaryFeedItemResponse;
import com.example.cp_main_be.domain.social.follow.domain.Follow;
import com.example.cp_main_be.domain.social.follow.domain.repository.FollowRepository;
import com.example.cp_main_be.global.dto.FeedItemResponse;
import com.example.cp_main_be.global.exception.UserNotFoundException;
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

  public List<FeedItemResponse> getFeed(UUID currentUserUuid, String filter) {
    Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"));

    User currentUser =
        userRepository
            .findByUuid(currentUserUuid)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

    // [추가] 1. 현재 사용자가 차단한 유저 ID 목록을 먼저 조회합니다.
    List<Long> blockedUserIds = userBlockRepository.findBlockedUserIdsByBlocker(currentUser);

    List<Diary> diaries;
    List<AvatarPost> avatarPosts;

    if ("following".equalsIgnoreCase(filter)) {
      List<User> followingUsers =
          followRepository.findByFollower(currentUser).stream().map(Follow::getFollowing).toList();

      // [수정] 2. 차단된 유저를 제외하고 조회
      diaries =
          diaryRepository.findByUserInAndIsPublicIsTrueAndUser_IdNotIn(
              followingUsers, blockedUserIds, pageable);
      avatarPosts =
          avatarPostRepository.findByUserInAndUser_IdNotIn(
              followingUsers, blockedUserIds, pageable);

    } else {
      // [수정] 2. 차단된 유저를 제외하고 조회
      diaries = diaryRepository.findByIsPublicIsTrueAndUser_IdNotIn(blockedUserIds, pageable);
      avatarPosts = avatarPostRepository.findAllByUser_IdNotIn(blockedUserIds, pageable);
    }

    // 2. 각 게시물을 해당하는 DTO로 변환
    Stream<DiaryFeedItemResponse> diaryStream = diaries.stream().map(DiaryFeedItemResponse::new);
    Stream<AvatarPostFeedItemResponse> avatarPostStream =
        avatarPosts.stream().map(AvatarPostFeedItemResponse::new);

    // 3. 두 스트림을 하나로 합친 후, 생성 시간(createdAt) 기준으로 내림차순 정렬
    List<FeedItemResponse> combinedFeed =
        Stream.concat(diaryStream, avatarPostStream)
            .sorted(Comparator.comparing(FeedItemResponse::getCreatedAt).reversed())
            .toList();

    return combinedFeed;
  }
}
