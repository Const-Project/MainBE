package com.example.cp_main_be.user.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
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

  private Long level;

  private Integer experiencePoints;

  private Integer temperatureScore;

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
    if (this.level == null) this.level = 1L; // 기본 레벨 설정
    if (this.experiencePoints == null) this.experiencePoints = 0; // 기본 경험치 설정
    if (this.temperatureScore == null) this.temperatureScore = 0; // 기본 온도 점수 설정
    if (this.status == null) this.status = UserStatus.ACTIVE; // 기본 상태 설정
  }

  @PreUpdate // 엔티티가 업데이트되기 전에 실행되는 콜백 메서드
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
