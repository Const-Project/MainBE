package com.example.cp_main_be.domain.member.userblock;

import com.example.cp_main_be.domain.member.user.domain.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBlock {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "blocker_user_id", nullable = false)
  private User blockerUser; // 차단을 한 사용자

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "blocked_user_id", nullable = false)
  private User blockedUser; // 차단을 당한 사용자

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Builder
  public UserBlock(User blockerUser, User blockedUser) {
    this.blockerUser = blockerUser;
    this.blockedUser = blockedUser;
  }
}
