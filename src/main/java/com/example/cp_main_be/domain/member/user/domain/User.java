package com.example.cp_main_be.domain.member.user.domain;

import com.example.cp_main_be.domain.content.avatar.domain.Avatar;
import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.social.bookmark.domain.Bookmark;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true) // User와 Avatar의 1:N 관계 설정
  @Builder.Default
  private List<Avatar> avatarList = new ArrayList<>();

  @Builder.Default private Long level = 1L; // 기본 레벨 설정

  @Builder.Default private Long experience = 0L; // 기본 경험치 설정

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  private UserStatus status;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  @JsonManagedReference
  private List<Garden> gardens = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  @JsonManagedReference
  private List<Diary> diaries = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  @JsonManagedReference
  private List<Bookmark> bookMarks = new ArrayList<>();

  @PrePersist // 엔티티가 영속화되기 전에 실행되는 콜백 메서드
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now(); // 최초 생성 시 updated_at도 설정
    if (this.status == null) this.status = UserStatus.ACTIVE; // 기본 상태 설정
  }

  @PreUpdate // 엔티티가 업데이트되기 전에 실행되는 콜백 메서드
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  // == 정보 수정 메서드 ==//
  public void updateProfile(String username, String profileImageUrl) {
    if (username != null) {
      this.username = username;
    }
    if (profileImageUrl != null) {
      this.profileImageUrl = profileImageUrl;
    }
  }

  // == 연관관계 편의 메서드 ==//
  public void addGarden(Garden garden) {
    this.gardens.add(garden);
  }

  // == 비즈니스 로직 메서드 (향후 확장용) ==//
  public void addExperience(int amount) {
    this.experience += amount;
    // TODO: 여기에 레벨업 확인 로직을 추가할 수 있습니다.
    // ex) if (this.experience >= getRequiredExperienceForNextLevel()) { levelUp(); }
  }
}
