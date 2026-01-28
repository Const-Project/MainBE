package com.example.cp_main_be.domain.member.notification.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String content;

  @Column(nullable = false)
  private String url;

  @Column(name = "thumbnail_url")
  private String thumbnailUrl;

  @Column(nullable = false)
  private boolean isRead;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType notificationType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User receiver;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  @Builder
  public Notification(
      User receiver,
      NotificationType notificationType,
      String content,
      String url,
      String thumbnailUrl,
      boolean isRead) {
    this.receiver = receiver;
    this.notificationType = notificationType;
    this.content = content;
    this.url = url;
    this.thumbnailUrl = thumbnailUrl;
    this.isRead = isRead;
  }

  public void read() {
    this.isRead = true;
  }
}
