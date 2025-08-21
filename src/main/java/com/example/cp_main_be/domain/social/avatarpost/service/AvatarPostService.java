package com.example.cp_main_be.domain.social.avatarpost.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import com.example.cp_main_be.domain.social.avatarpost.dto.response.PostInfoResponse;
import com.example.cp_main_be.domain.social.bookmark.domain.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvatarPostService {

  private final AvatarPostRepository avatarPostRepository;
  private final UserRepository userRepository;
  private final BookmarkRepository bookmarkRepository;

  public PostInfoResponse getPostInfoWithBookmarkStatus(Long postId, User currentUser) {
    // 1. 포스트 정보 조회
    AvatarPost postById =
        avatarPostRepository
            .findByIdWithComments(postId) // 댓글을 함께 조회하는 메서드 사용 (N+1 문제 방지)
            .orElseThrow(() -> new IllegalArgumentException("해당 포스트를 찾을 수 없습니다."));

    // 2. 현재 유저가 포스트를 북마크했는지 확인
    boolean isBookmarked =
        bookmarkRepository.existsByUserAndAvatarPost(
            currentUser, postById); // exists... 쿼리가 더 효율적입니다.

    // 3. PostInfoResponse DTO 생성 및 반환
    // 이미지 저장은 미구현
    return PostInfoResponse.builder()
        .imageUrl(postById.getImageUrl())
        .likeCount(postById.getLikeCount())
        .comments(postById.getComments())
        .isBookmarked(isBookmarked)
        .build();
  }
}
