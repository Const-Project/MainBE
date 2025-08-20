package com.example.cp_main_be.domain.social.avatarpost.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.comment.domain.Comment;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

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

  @Lob
  @Column(name = "caption")
  private String caption;

  @Column(name = "like_count")
  @Builder.Default
  private int likeCount = 0;

  @OneToMany(mappedBy = "avatarPost", cascade = CascadeType.ALL)
  private List<Comment> comments;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  public void increaseLikeCount() {
    this.likeCount++;
  }

  public void decreaseLikeCount() {
    this.likeCount = Math.max(0, this.likeCount - 1);
  }
}
