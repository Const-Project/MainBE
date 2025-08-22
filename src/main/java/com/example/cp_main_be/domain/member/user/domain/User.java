package com.example.cp_main_be.domain.member.user.domain;

import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.social.bookmark.domain.Bookmark;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
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

  @Column(unique = true)
  private UUID uuid;

  @Enumerated(EnumType.STRING) // [추가] Enum 타입을 DB에 저장할 때 문자열로 저장
  @Column(nullable = false)
  private Role role; // 역할 필드 추가

  @Column(nullable = false)
  private String nickname;

  private String email;

  private String passwordHash;

  private String profileImageUrl;

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true) // User와 Avatar의 1:N 관계 설정
  @Builder.Default
  private List<Avatar> avatarList = new ArrayList<>();

  @Builder.Default private Integer level = 1; // 기본 레벨 설정

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

  @Builder.Default private Boolean notificationEnabled = true;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    if (this.status == null) this.status = UserStatus.ACTIVE;
    if (this.role == null) this.role = Role.USER; // [추가] 기본 역할을 USER로 설정
  }

  @PreUpdate // 엔티티가 업데이트되기 전에 실행되는 콜백 메서드
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  // == 정보 수정 메서드 ==//
  public void updateProfile(String nickname, String profileImageUrl) {
    if (nickname != null) {
      this.nickname = nickname;
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

  public void levelUp(long remainingExp) {
    this.level++;
    this.experience = remainingExp;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // 이 사용자가 가진 역할을 기반으로 권한 목록을 반환
    return Collections.singletonList(new SimpleGrantedAuthority(this.role.getKey()));
  }

  @Override
  public String getPassword() {
    return this.passwordHash; // passwordHash 필드를 반환
  }

  @Override
  public String getUsername() {
    // Spring Security에서 username은 고유 식별자를 의미합니다.
    // 여기서는 uuid를 사용하겠습니다.
    return this.uuid.toString();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true; // 계정이 만료되지 않았음
  }

  @Override
  public boolean isAccountNonLocked() {
    return true; // 계정이 잠기지 않았음
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true; // 자격 증명(비밀번호)이 만료되지 않았음
  }

  @Override
  public boolean isEnabled() {
    return this.status == UserStatus.ACTIVE; // 활성 상태인 경우에만 계정 활성화
  }
}
