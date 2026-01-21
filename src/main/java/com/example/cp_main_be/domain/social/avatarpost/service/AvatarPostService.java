package com.example.cp_main_be.domain.social.avatarpost.service;

import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import com.example.cp_main_be.domain.social.avatarpost.dto.PostInfoResponse;
import com.example.cp_main_be.domain.social.like.avatar_post.repository.AvatarPostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AvatarPostService {

  private final AvatarPostRepository avatarPostRepository;
  private final AvatarPostLikeRepository avatarPostLikeRepository;

  public PostInfoResponse getAvatarPostInfo(Long postId, User currentUser) {
    // 1. N+1 문제를 해결하기 위해 연관된 엔티티(작성자, 댓글, 댓글 작성자)를 함께 조회합니다.
    var post =
        avatarPostRepository
            .findByIdWithDetails(postId)
            .orElseThrow(() -> new IllegalArgumentException("해당 포스트를 찾을 수 없습니다."));

    // 2. 현재 사용자의 '좋아요' 여부를 확인합니다.
    boolean isLiked = false;
    if (currentUser != null) {
      isLiked = avatarPostLikeRepository.existsByUserAndAvatarPost(currentUser, post);
    }

    // 3. 조회된 엔티티와 '좋아요' 여부를 DTO로 변환하여 반환합니다.
    return PostInfoResponse.from(post, isLiked);
  }

  public PostInfoResponse createAvatarPost(Avatar avatar, User currentUser) {
    AvatarPost avatarPost = avatarPostRepository.save(AvatarPost.from(avatar, currentUser));
    return PostInfoResponse.from(avatarPost, true);
  }
}
