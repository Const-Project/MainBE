package com.example.cp_main_be.user.domain;

import com.example.cp_main_be.avatar.domain.Avatar;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long id;

  @Column(unique = true)
  private UUID uuid;

  @Column(nullable = false)
  private String username;

  private String email;

  private String passwordHash;

  private String profileImageUrl;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Avatar> avatars = new ArrayList<>();

  @Builder.Default private Long level = 1L; // 기본 레벨 설정

  @Builder.Default private Integer experiencePoints = 0; // 기본 경험치 설정

  @Builder.Default private Integer temperatureScore = 0; // 기본 온도 점수 설정

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Enumerated(EnumType.STRING)
  private UserStatus status;

  @PrePersist // 엔티티가 영속화되기 전에 실행되는 콜백 메서드
  protected void onCreate() {
    this.uuid = UUID.randomUUID();
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now(); // 최초 생성 시 updated_at도 설정
    if (this.status == null) this.status = UserStatus.ACTIVE; // 기본 상태 설정
  }

  @PreUpdate // 엔티티가 업데이트되기 전에 실행되는 콜백 메서드
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
