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
import com.example.cp_main_be.domain.social.feed.dto.response.FeedScrollResponse;
import com.example.cp_main_be.domain.social.follow.domain.Follow;
import com.example.cp_main_be.domain.social.follow.domain.repository.FollowRepository;
import com.example.cp_main_be.domain.social.like.avatar_post.repository.AvatarPostLikeRepository;
import com.example.cp_main_be.domain.social.like.diary.repository.DiaryLikeRepository;
import com.example.cp_main_be.global.dto.FeedItemResponse;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
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
  private final DiaryLikeRepository diaryLikeRepository;
  private final AvatarPostLikeRepository avatarPostLikeRepository;
  private final CommentRepository commentRepository;

  /** 시간순 정렬된 피드 조회 (커서 기반) */
  public List<FeedResponse> getFeed(
      UUID currentUserUuid, String filter, LocalDateTime cursor, int size) {

    if (cursor == null) {
      cursor = LocalDateTime.now();
    }

    Pageable pageable = PageRequest.of(0, size);

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

    // 두 정렬된 리스트를 merge하여 size개만 반환
    return mergeSortedFeeds(diaries, avatarPosts, size);
  }

  /** 랜덤 피드 조회 (제외 목록 기반) */
  public FeedScrollResponse getRandomFeed(
      List<Long> excludeDiaryIds, List<Long> excludeAvatarPostIds, int size) {

    if (size <= 0) {
      return new FeedScrollResponse(Collections.emptyList(), false);
    }

    // null 체크
    List<Long> safeDiaryIds = (excludeDiaryIds != null) ? excludeDiaryIds : Collections.emptyList();
    List<Long> safeAvatarIds =
        (excludeAvatarPostIds != null) ? excludeAvatarPostIds : Collections.emptyList();

    // 각 소스에서 가져올 항목 수 계산
    int diarySize = size / 2;
    int avatarPostSize = size - diarySize;

    // 충분한 양을 가져오기 위해 2배로 요청
    int diaryFetchSize = diarySize * 2;
    int avatarPostFetchSize = avatarPostSize * 2;

    // 각 소스에서 랜덤 ID 조회 (제외 목록 포함)
    List<Long> randomDiaryIds =
        (diaryFetchSize > 0)
            ? diaryRepository.findRandomPublicDiaryIdsExcluding(safeDiaryIds, diaryFetchSize)
            : Collections.emptyList();

    List<Long> randomAvatarPostIds =
        (avatarPostFetchSize > 0)
            ? avatarPostRepository.findRandomPublicAvatarPostIdsExcluding(
                safeAvatarIds, avatarPostFetchSize)
            : Collections.emptyList();

    // 각 소스의 데이터를 FeedItemResponse로 변환
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

    // 랜덤 섞기
    Collections.shuffle(feedItems);

    // size만큼만 반환
    List<FeedItemResponse> result = feedItems.stream().limit(size).toList();

    // 더 가져올 데이터가 있는지 확인 (각 소스별로 체크)
    boolean hasDiaryMore = randomDiaryIds.size() >= diaryFetchSize;
    boolean hasAvatarPostMore = randomAvatarPostIds.size() >= avatarPostFetchSize;
    boolean hasMore = hasDiaryMore || hasAvatarPostMore;

    return new FeedScrollResponse(result, hasMore);
  }

  /** 시간순 정렬된 두 리스트를 병합하여 size개만 반환 */
  private List<FeedResponse> mergeSortedFeeds(
      List<Diary> diaries, List<AvatarPost> avatarPosts, int size) {

    List<FeedResponse> result = new ArrayList<>(size);
    int i = 0, j = 0;

    while (result.size() < size && (i < diaries.size() || j < avatarPosts.size())) {
      if (i >= diaries.size()) {
        // Diary 소진, AvatarPost만 추가
        result.add(FeedResponse.from(avatarPosts.get(j++)));
      } else if (j >= avatarPosts.size()) {
        // AvatarPost 소진, Diary만 추가
        result.add(FeedResponse.from(diaries.get(i++)));
      } else {
        // 둘 다 있으면 시간 비교
        LocalDateTime diaryTime = diaries.get(i).getCreatedAt();
        LocalDateTime postTime = avatarPosts.get(j).getCreatedAt();

        if (diaryTime.isAfter(postTime)) {
          result.add(FeedResponse.from(diaries.get(i++)));
        } else {
          result.add(FeedResponse.from(avatarPosts.get(j++)));
        }
      }
    }

    return result;
  }

  /** ID 목록을 기반으로 엔티티와 관련 데이터(좋아요, 댓글 수)를 조회하고 FeedItemResponse로 매핑 */
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

    // 좋아요 및 댓글 수를 타입에 따라 분기하여 조회
    Map<Long, Long> likeCounts;
    List<CommentCountDto> commentCountDtos;

    if ("DIARY".equalsIgnoreCase(type)) {
      likeCounts = diaryLikeRepository.countLikesByDiaryIds(ids);
      commentCountDtos = commentRepository.countCommentsByDiaryIds(ids);
    } else if ("AVATAR_POST".equalsIgnoreCase(type)) {
      likeCounts = avatarPostLikeRepository.countLikesByAvatarPostIds(ids);
      commentCountDtos = commentRepository.countCommentsByAvatarPostIds(ids);
    } else {
      likeCounts = Collections.emptyMap();
      commentCountDtos = Collections.emptyList();
    }

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

  @FunctionalInterface
  interface TriFunction<T, U, V, R> {
    R apply(T t, U u, V v);
  }
}
