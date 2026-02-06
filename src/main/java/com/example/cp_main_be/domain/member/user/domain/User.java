package com.example.cp_main_be.domain.member.user.domain;

import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import com.example.cp_main_be.domain.mission.wishTree.WishTree;
import com.example.cp_main_be.domain.social.bookmark.domain.Bookmark;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long id;

  @Column(name = "last_visited_garden_id")
  private Long lastVisitedGardenId;

  @Column(name = "last_accessed_at")
  private LocalDateTime lastAccessedAt;

  public void updateLastVisitedGarden(Long gardenId) {
    this.lastVisitedGardenId = gardenId;
  }

  public void updateLastAccessedAt(LocalDateTime accessedAt) {
    this.lastAccessedAt = accessedAt;
  }

  @Column(unique = true)
  private UUID uuid;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(nullable = false)
  private String nickname;

  private String email;

  private String passwordHash;

  private String profileImageUrl;

  @Column(name = "oauth_provider")
  private String oauthProvider;

  @Column(name = "oauth_subject")
  private String oauthSubject;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Avatar> avatarList = new ArrayList<>();

  // [제거] level, experience 관련 필드와 메서드를 모두 삭제합니다.

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

  @Builder.Default private Boolean notificationEnabled = true;

  @Builder.Default private Boolean marketingConsent = false; // [추가] 마케팅 수신 동의

  @Builder.Default private Integer unlockableGardenCount = 0;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private WishTree wishTree;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    if (this.status == null) this.status = UserStatus.ACTIVE;
    if (this.role == null) this.role = Role.USER;
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public void updateProfile(String nickname, String profileImageUrl) {
    if (nickname != null) {
      this.nickname = nickname;
    }
    if (profileImageUrl != null) {
      this.profileImageUrl = profileImageUrl;
    }
  }

  public void addGarden(Garden garden) {
    this.gardens.add(garden);
    garden.setUser(this);
  }

  public void incrementUnlockableGardenCount() {
    this.unlockableGardenCount++;
  }

  public void decrementUnlockableGardenCount() {
    if (this.unlockableGardenCount == null || this.unlockableGardenCount <= 0) {
      throw new com.example.cp_main_be.global.common.CustomApiException(
          com.example.cp_main_be.global.common.ErrorCode.INVALID_REQUEST,
          "unlockableGardenCount는 0보다 작을 수 없습니다.");
    }
    this.unlockableGardenCount--;
  }

  public void addAvatar(Avatar avatar) {
    this.avatarList.add(avatar);
    avatar.setUser(this);
  }

  // [제거] addExperience, levelUp 메서드 삭제

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority(this.role.getKey()));
  }

  @Override
  public String getPassword() {
    return this.passwordHash;
  }

  @Override
  public String getUsername() {
    return this.uuid.toString();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return this.status == UserStatus.ACTIVE;
  }
}
