package com.example.cp_main_be.domain.social.like.avatar_post.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "avatar_post_likes",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_avatar_post_like",
            columnNames = {"user_id", "avatar_post_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvatarPostLike {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "avatar_post_id", nullable = false)
  private AvatarPost avatarPost;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  @Builder
  public AvatarPostLike(User user, AvatarPost avatarPost) {
    this.user = user;
    this.avatarPost = avatarPost;
  }
}
