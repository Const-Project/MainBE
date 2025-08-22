// package com.example.cp_main_be.domain.social.avatarpost.service;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.BDDMockito.given;
// import static org.mockito.Mockito.verify;
//
// import com.example.cp_main_be.domain.member.user.domain.User;
// import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
// import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
// import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
// import com.example.cp_main_be.domain.social.avatarpost.dto.PostInfoResponse;
// import com.example.cp_main_be.domain.social.bookmark.domain.Bookmark;
// import com.example.cp_main_be.domain.social.bookmark.domain.repository.BookmarkRepository;
// import java.util.Collections;
// import java.util.Optional;
// import java.util.UUID;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
//
// @ExtendWith(MockitoExtension.class)
// class AvatarPostServiceTest {
//
//  @Mock private UserRepository userRepository;
//
//  @Mock private AvatarPostRepository avatarPostRepository;
//
//  @Mock private BookmarkRepository bookmarkRepository;
//
//  @InjectMocks private AvatarPostService avatarPostService;
//
//  @DisplayName("포스트 정보 조회 성공: 북마크된 포스트")
//  @Test
//  void getPostInfoWithBookmarkStatus_bookmarkedPost_success() {
//    // Given
//    Long postId = 1L;
//    User currentUser = User.builder().id(100L).nickname("currentUser").build();
//    AvatarPost mockPost =
//        AvatarPost.builder()
//            .id(postId)
//            .caption("포스트 내용")
//            .likeCount(0) // 좋아요 목록 (size() 호출 대비)
//            .comments(Collections.emptyList()) // 댓글 목록 (comments 호출 대비)
//            .build();
//
//    Bookmark mockBookmark = new Bookmark(currentUser, mockPost); // 북마크 객체 생성
//
//    // Mocking: avatarPostRepository.findById가 포스트를 반환하도록 설정
//    given(avatarPostRepository.findById(postId)).willReturn(Optional.of(mockPost));
//    // Mocking: bookmarkRepository.findByUserAndAvatarPost가 북마크를 찾도록 설정
//    given(bookmarkRepository.findByUserAndAvatarPost(currentUser, mockPost))
//        .willReturn(Optional.of(mockBookmark));
//
//    // When
//    PostInfoResponse result = avatarPostService.getPostInfoWithBookmarkStatus(postId,
// currentUser);
//
//    // Then
//    assertThat(result).isNotNull();
//    assertThat(result.isBookmarked()).isTrue(); // 북마크됨을 검증
//    assertThat(result.getLikeCount()).isEqualTo(0); // 좋아요 수 검증 (Collections.emptyList().size())
//    assertThat(result.getComments()).isEmpty(); // 댓글 목록 검증
//
//    // verify: 각 리포지토리 메서드가 올바른 인자로 호출되었는지 확인
//    verify(avatarPostRepository).findById(postId);
//    verify(bookmarkRepository).findByUserAndAvatarPost(currentUser, mockPost);
//  }
//
//  @DisplayName("포스트 정보 조회 성공: 북마크되지 않은 포스트")
//  @Test
//  void getPostInfoWithBookmarkStatus_notBookmarkedPost_success() {
//    // Given
//    Long postId = 2L;
//    User currentUser =
//        User.builder().id(101L).uuid(UUID.randomUUID()).nickname("currentUser2").build();
//    AvatarPost mockPost =
//        AvatarPost.builder()
//            .id(postId)
//            .caption("다른 내용")
//            .likeCount(0)
//            .comments(Collections.emptyList())
//            .build();
//
//    // Mocking: avatarPostRepository.findById가 포스트를 반환하도록 설정
//    given(avatarPostRepository.findById(postId)).willReturn(Optional.of(mockPost));
//    // Mocking: bookmarkRepository.findByUserAndAvatarPost가 북마크를 찾지 못하도록 설정
//    given(bookmarkRepository.findByUserAndAvatarPost(currentUser, mockPost))
//        .willReturn(Optional.empty());
//
//    // When
//    PostInfoResponse result = avatarPostService.getPostInfoWithBookmarkStatus(postId,
// currentUser);
//
//    // Then
//    assertThat(result).isNotNull();
//    assertThat(result.isBookmarked()).isFalse(); // 북마크되지 않음을 검증
//    assertThat(result.getLikeCount()).isEqualTo(0);
//    assertThat(result.getComments()).isEmpty();
//
//    // verify: 각 리포지토리 메서드가 올바른 인자로 호출되었는지 확인
//    verify(avatarPostRepository).findById(postId);
//    verify(bookmarkRepository).findByUserAndAvatarPost(currentUser, mockPost);
//  }
// }
