package com.example.cp_main_be.domain.social.avatarpost.domain;

import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.comment.domain.Comment;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class AvatarPost {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "avatar_post_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // [핵심 수정] 어떤 Avatar를 포스팅하는지에 대한 직접적인 참조
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "avatar_id", nullable = false)
  private Avatar avatar;

  @Column(name = "image_url")
  private String imageUrl;

  @Column(name = "caption", length = 2000)
  private String caption;

  @Column(name = "like_count")
  @Builder.Default
  private int likeCount = 0;

  @OneToMany(mappedBy = "avatarPost", cascade = CascadeType.ALL)
  private List<Comment> comments;

  @Column(name = "created_at", nullable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  @UpdateTimestamp
  private LocalDateTime updatedAt;

  public void increaseLikeCount() {
    this.likeCount++;
  }

  public void decreaseLikeCount() {
    this.likeCount = Math.max(0, this.likeCount - 1);
  }

  public static AvatarPost from(Avatar avatar, User user) {
    return AvatarPost.builder()
        .user(user)
        .avatar(avatar)
        .imageUrl(avatar.getImageUrl())
        .caption("")
        .comments(new ArrayList<Comment>())
        .build();
  }
}
