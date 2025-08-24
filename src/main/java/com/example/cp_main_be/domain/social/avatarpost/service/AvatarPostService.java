package com.example.cp_main_be.domain.social.avatarpost.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import com.example.cp_main_be.domain.social.avatarpost.dto.PostInfoResponse;
import com.example.cp_main_be.domain.social.bookmark.domain.repository.BookmarkRepository;
import com.example.cp_main_be.domain.social.comment.domain.repository.CommentRepository;
import com.example.cp_main_be.domain.social.like.domain.repository.LikeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AvatarPostService {

  private final AvatarPostRepository avatarPostRepository;
  private final LikeRepository likeRepository;

  public PostInfoResponse getAvatarPostInfo(Long postId, User currentUser) {
    // 1. 포스트 정보 조회
    AvatarPost postById =
        avatarPostRepository
            .findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("해당 포스트를 찾을 수 없습니다."));

    // 좋아요 여부 확인
    boolean isLiked = likeRepository.existsByUserIdAndTargetId(currentUser.getId(), postId);


    // 응답 형식에 맞게 comment DTO 생성
    List<PostInfoResponse.CommentResponseDTO> comments = postById.getComments().stream()
            .map(comment -> PostInfoResponse.CommentResponseDTO.builder()
                    .commentId(comment.getId())
                    .content(comment.getContent())
                    .profileImageUrl(comment.getWriter().getProfileImageUrl())
                    .writer(comment.getWriter().getNickname())
                    .build()).toList();


    return PostInfoResponse.builder()
            .id(postById.getId())
            .title(postById.getUser().getNickname())
            .content(postById.getCaption())
            .imageUrl(postById.getImageUrl())
            .isLiked(isLiked)
            .isPublic(true)
            .commentCount(comments.size())
            .createdAt(postById.getCreatedAt())
            .updatedAt(postById.getUpdatedAt())
            .comment(comments)
            .build();
  }

//  private Long id;
//  private String title;
//  private String content;
//  private String imageUrl;
//  private boolean isLiked;
//  private int likeCount;
//  private int commentCount;
//  private List<PostInfoResponse.CommentResponseDTO> comment;
//  private LocalDateTime createdAt;
//  private LocalDateTime updatedAt;
}
