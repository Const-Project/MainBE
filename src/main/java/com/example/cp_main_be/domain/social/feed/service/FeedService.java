package com.example.cp_main_be.domain.social.feed.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import com.example.cp_main_be.domain.social.avatarpost.dto.AvatarPostFeedItemResponse;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.domain.repository.DiaryRepository;
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

  public List<FeedItemResponse> getFeed(UUID currentUserUuid, String filter) {
    // 실제로는 페이징 처리가 필요하지만, 여기서는 최신 100개씩 조회하는 것으로 가정
    Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"));

    List<Diary> diaries;
    List<AvatarPost> avatarPosts;

    // "following" 필터가 적용된 경우
    if ("following".equalsIgnoreCase(filter)) {
      // 1. 현재 사용자 엔티티를 조회합니다.
      User currentUser =
          userRepository
              .findByUuid(currentUserUuid)
              .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

      // 2. 현재 사용자가 팔로우하는 모든 관계를 조회합니다.
      List<Follow> follows = followRepository.findByFollower(currentUser);

      // 3. 팔로우 관계에서 '팔로잉 당하는' 사용자(following)들의 리스트를 추출합니다.
      List<User> followingUsers = follows.stream().map(Follow::getFollowing).toList();

      // 4. 팔로우하는 사용자들이 작성한 게시물만 조회합니다.
      diaries = diaryRepository.findByUserInAndIsPublicIsTrue(followingUsers, pageable);
      avatarPosts = avatarPostRepository.findByUserIn(followingUsers, pageable);

    } else { // 필터가 없으면 모든 공개 게시물 조회
      diaries = diaryRepository.findByIsPublicIsTrue(pageable);
      avatarPosts = avatarPostRepository.findAll(pageable).getContent();
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
