package com.example.cp_main_be.domain.social.feed.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import com.example.cp_main_be.domain.mission.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.mission.diary.dto.response.DiaryFeedItemResponse;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import com.example.cp_main_be.domain.social.avatarpost.dto.AvatarPostFeedItemResponse;
import com.example.cp_main_be.domain.social.comment.domain.repository.CommentRepository;
import com.example.cp_main_be.domain.social.comment.dto.CommentCountDto;
import com.example.cp_main_be.domain.social.feed.dto.response.FeedResponse;
import com.example.cp_main_be.domain.social.follow.domain.Follow;
import com.example.cp_main_be.domain.social.follow.domain.repository.FollowRepository;
import com.example.cp_main_be.domain.social.like.domain.repository.LikeRepository;
import com.example.cp_main_be.global.dto.FeedItemResponse;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
  private final LikeRepository likeRepository;
  private final CommentRepository commentRepository;

  // FeedService.java

  // public List<FeedResponse> getFeed(UUID currentUserUuid, String filter, int page, int size) {
  // ... }
  // 위 메서드를 아래와 같이 변경합니다.

  public List<FeedResponse> getFeed(
      UUID currentUserUuid, String filter, LocalDateTime cursor, int size) {
    // 커서가 없으면(최초 요청) 현재 시간으로 설정
    if (cursor == null) {
      cursor = LocalDateTime.now();
    }
    Pageable pageable = PageRequest.of(0, size); // 각 소스에서 size만큼만 가져옴

    User currentUser =
        userRepository
            .findByUuid(currentUserUuid)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

    List<Diary> diaries;
    List<AvatarPost> avatarPosts;

    if ("following".equalsIgnoreCase(filter)) {
      List<User> followingUsers =
          followRepository.findByFollower(currentUser).stream().map(Follow::getFollowing).toList();

      diaries = diaryRepository.findFollowingDiariesWithCursor(followingUsers, cursor, pageable);
      avatarPosts =
          avatarPostRepository.findByUserInAndCreatedAtBeforeOrderByCreatedAtDesc(
              followingUsers, cursor, pageable);
    } else {
      diaries = diaryRepository.findPublicDiariesWithCursor(cursor, pageable);
      avatarPosts =
          avatarPostRepository.findByCreatedAtBeforeOrderByCreatedAtDesc(cursor, pageable);
    }

    // 두 스트림을 합치고, size만큼만 잘라낸 후, DTO로 변환
    return Stream.concat(
            diaries.stream().map(FeedResponse::from), avatarPosts.stream().map(FeedResponse::from))
        .sorted(Comparator.comparing(FeedResponse::createdAt).reversed())
        .limit(size)
        .toList();
  }

  public List<FeedItemResponse> getRandomFeed(Long excludePostId, int page, int size) {
    if (size <= 0) {
      return Collections.emptyList();
    }

    // 1. 각 소스에서 가져올 항목 수를 계산합니다.
    int diarySize = size / 2;
    int avatarPostSize = size - diarySize;

    // 2. 각 소스에서 랜덤 ID 목록을 조회합니다.
    List<Long> randomDiaryIds =
        (diarySize > 0)
            ? diaryRepository.findRandomPublicDiaryIds(
                excludePostId, PageRequest.of(page, diarySize))
            : Collections.emptyList();

    List<Long> randomAvatarPostIds =
        (avatarPostSize > 0)
            ? avatarPostRepository.findRandomPublicAvatarPostIds(
                excludePostId, PageRequest.of(page, avatarPostSize))
            : Collections.emptyList();

    // 3. 각 소스의 데이터를 가져와 FeedItemResponse로 변환합니다.
    List<FeedItemResponse> feedItems = new ArrayList<>();

    // Diary 처리
    List<DiaryFeedItemResponse> diaryItems =
        fetchAndMapFeedItems(
            randomDiaryIds,
            "DIARY",
            diaryRepository::findAllByIdIn,
            Diary::getId,
            (diary, likeCount, commentCount) ->
                new DiaryFeedItemResponse(diary, likeCount, commentCount));
    feedItems.addAll(diaryItems);

    // AvatarPost 처리
    List<AvatarPostFeedItemResponse> avatarPostItems =
        fetchAndMapFeedItems(
            randomAvatarPostIds,
            "AVATAR_POST",
            avatarPostRepository::findAllByIdIn,
            AvatarPost::getId,
            (post, likeCount, commentCount) ->
                new AvatarPostFeedItemResponse(post, likeCount, commentCount));
    feedItems.addAll(avatarPostItems);

    // 4. 최종 리스트를 섞어서 순서를 랜덤하게 만듭니다.
    Collections.shuffle(feedItems);

    return feedItems;
  }

  /**
   * ID 목록을 기반으로 엔티티와 관련 데이터(좋아요, 댓글 수)를 조회하고 FeedItemResponse로 매핑하는 공통 메서드
   *
   * @param ids 조회할 엔티티 ID 목록
   * @param type 대상 타입 문자열 ("DIARY", "AVATAR_POST")
   * @param entityFetcher ID 목록으로 엔티티 목록을 조회하는 함수
   * @param idExtractor 엔티티에서 ID를 추출하는 함수
   * @param responseMapper 엔티티와 카운트 정보로 최종 DTO를 생성하는 함수
   * @return 변환된 FeedItemResponse 목록
   * @param <T> 엔티티 타입 (Diary, AvatarPost)
   * @param <R> 응답 DTO 타입 (DiaryFeedItemResponse, AvatarPostFeedItemResponse)
   */
  private <T, R extends FeedItemResponse> List<R> fetchAndMapFeedItems(
      List<Long> ids,
      String type,
      Function<List<Long>, List<T>> entityFetcher,
      Function<T, Long> idExtractor,
      TriFunction<T, Long, Integer, R> responseMapper) {

    if (ids == null || ids.isEmpty()) {
      return Collections.emptyList();
    }

    // 엔티티 조회
    List<T> entities = entityFetcher.apply(ids);

    // --- 이 부분이 수정되었습니다 ---
    // 좋아요 및 댓글 수를 타입에 따라 분기하여 조회합니다.
    Map<Long, Long> likeCounts;
    List<CommentCountDto> commentCountDtos;

    if ("DIARY".equalsIgnoreCase(type)) {
      likeCounts =
          likeRepository.countLikesByTargetIds(ids, "DIARY"); // likeRepository도 분리되었다면 수정 필요
      commentCountDtos = commentRepository.countCommentsByDiaryIds(ids);
    } else if ("AVATAR_POST".equalsIgnoreCase(type)) {
      likeCounts =
          likeRepository.countLikesByTargetIds(ids, "AVATAR_POST"); // likeRepository도 분리되었다면 수정 필요
      commentCountDtos = commentRepository.countCommentsByAvatarPostIds(ids);
    } else {
      likeCounts = Collections.emptyMap();
      commentCountDtos = Collections.emptyList();
    }
    // --- 수정 끝 ---

    Map<Long, Long> commentCounts =
        commentCountDtos.stream()
            .collect(Collectors.toMap(CommentCountDto::getTargetId, CommentCountDto::getCount));

    // 엔티티를 최종 DTO로 변환
    return entities.stream()
        .map(
            entity -> {
              long entityId = idExtractor.apply(entity);
              long likeCount = likeCounts.getOrDefault(entityId, 0L);
              int commentCount = commentCounts.getOrDefault(entityId, 0L).intValue();
              return responseMapper.apply(entity, likeCount, commentCount);
            })
        .toList();
  }

  // 3개의 파라미터를 받는 함수형 인터페이스가 기본 API에 없으므로 직접 정의합니다.
  @FunctionalInterface
  interface TriFunction<T, U, V, R> {
    R apply(T t, U u, V v);
  }
}
