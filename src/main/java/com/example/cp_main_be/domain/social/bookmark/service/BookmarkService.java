package com.example.cp_main_be.domain.social.bookmark.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.domain.member.userblock.UserBlockRepository;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import com.example.cp_main_be.domain.social.avatarpost.dto.AvatarPostFeedItemResponse;
import com.example.cp_main_be.domain.social.bookmark.domain.Bookmark;
import com.example.cp_main_be.domain.social.bookmark.domain.repository.BookmarkRepository;
import com.example.cp_main_be.domain.social.comment.domain.repository.CommentRepository;
import com.example.cp_main_be.domain.social.comment.dto.CommentCountDto;
import com.example.cp_main_be.domain.social.like.avatar_post.repository.AvatarPostLikeRepository;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

  private final BookmarkRepository bookmarkRepository;
  private final UserService userService;
  private final AvatarPostRepository avatarPostRepository;
  private final AvatarPostLikeRepository avatarPostLikeRepository;
  private final CommentRepository commentRepository;
  private final UserBlockRepository userBlockRepository;

  @Transactional
  public void addBookmark(Long userId, Long postId) {
    User user = userService.findById(userId);
    AvatarPost post =
        avatarPostRepository
            .findById(postId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.POST_NOT_FOUND));

    if (userBlockRepository.existsByBlockerUserAndBlockedUser(post.getUser(), user)
        || userBlockRepository.existsByBlockerUserAndBlockedUser(user, post.getUser())) {
      throw new CustomApiException(ErrorCode.ACCESS_DENIED, "차단 상태에서는 북마크할 수 없습니다.");
    }

    if (bookmarkRepository.existsByUserAndAvatarPost(user, post)) {
      throw new CustomApiException(ErrorCode.INVALID_REQUEST, "이미 북마크한 게시물입니다.");
    }

    Bookmark bookmark =
        Bookmark.builder().user(user).avatarPost(post).targetType("AVATAR_POST").build();
    bookmarkRepository.save(bookmark);
  }

  @Transactional
  public void removeBookmark(Long userId, Long postId) {
    User user = userService.findById(userId);
    AvatarPost post =
        avatarPostRepository
            .findById(postId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.POST_NOT_FOUND));

    Bookmark bookmark =
        bookmarkRepository
            .findByUserAndAvatarPost(user, post)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND, "북마크를 찾을 수 없습니다."));

    bookmarkRepository.delete(bookmark);
  }

  @Transactional(readOnly = true)
  public List<AvatarPostFeedItemResponse> getMyBookmarks(Long userId, int page, int size) {
    User user = userService.findById(userId);
    List<Long> blockedUserIds = userBlockRepository.findBlockedUserIdsByBlocker(user);

    int safePage = Math.max(0, page);
    int safeSize = Math.max(1, size);
    Pageable pageable = PageRequest.of(safePage, safeSize);
    List<Bookmark> bookmarks =
        bookmarkRepository.findAllByUserOrderByCreatedAtDesc(user, pageable).getContent();
    if (bookmarks.isEmpty()) {
      return Collections.emptyList();
    }

    List<Long> postIds =
        bookmarks.stream()
            .map(Bookmark::getAvatarPost)
            .filter(post -> post != null)
            .map(AvatarPost::getId)
            .toList();

    if (postIds.isEmpty()) {
      return Collections.emptyList();
    }

    Map<Long, AvatarPost> postMap =
        avatarPostRepository.findAllByIdIn(postIds).stream()
            .filter(post -> !blockedUserIds.contains(post.getUser().getId()))
            .collect(Collectors.toMap(AvatarPost::getId, post -> post));

    Map<Long, Long> likeCounts = avatarPostLikeRepository.countLikesByAvatarPostIds(postIds);
    List<CommentCountDto> commentCountDtos =
        commentRepository.countCommentsByAvatarPostIds(postIds);
    Map<Long, Long> commentCounts =
        commentCountDtos.stream()
            .collect(Collectors.toMap(CommentCountDto::getTargetId, CommentCountDto::getCount));

    return bookmarks.stream()
        .map(Bookmark::getAvatarPost)
        .filter(post -> post != null && postMap.containsKey(post.getId()))
        .map(
            post -> {
              long likeCount = likeCounts.getOrDefault(post.getId(), 0L);
              long commentCount = commentCounts.getOrDefault(post.getId(), 0L);
              return new AvatarPostFeedItemResponse(
                  postMap.get(post.getId()), likeCount, commentCount);
            })
        .toList();
  }
}
