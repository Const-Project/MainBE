package com.example.cp_main_be.domain.garden.garden.domain;

import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "garden")
public class Garden {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @JsonBackReference
  private User user;

  // [추가] Garden이 어떤 Avatar를 가지고 있는지에 대한 관계
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "avatar_id")
  private Avatar avatar;

  @Column(nullable = false)
  private Integer slotNumber;

  @Column(nullable = false)
  @Builder.Default
  private Integer waterCount = 0;

  @Column(nullable = false)
  @Builder.Default
  private Integer sunlightCount = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "garden_background_id")
  private GardenBackground gardenBackground;

  @Column(name = "last_watered_by_owner_at")
  private LocalDateTime lastWateredByOwnerAt;

  @Column(name = "last_watered_by_friend_at")
  private LocalDateTime lastWateredByFriendAt;

  @Column(name = "last_sunlight_received_at")
  private LocalDateTime lastSunlightReceivedAt;

  private boolean isLocked;

  @CreatedDate
  @Column(nullable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Builder
  public Garden(User user, Integer slotNumber, GardenBackground gardenBackground, Avatar avatar) {
    this.user = user;
    this.slotNumber = slotNumber;
    this.waterCount = 0;
    this.sunlightCount = 0;
    this.gardenBackground = gardenBackground;
    this.avatar = avatar; // [추가]
  }

  @Transient // DB에 저장하지 않는, 계산된 필드임을 명시
  public boolean isWaterableByOwner() {
    if (this.lastWateredByOwnerAt == null) {
      return true; // 한 번도 물을 준 적이 없다면 항상 가능
    }
    LocalDateTime nextWaterableTime = this.lastWateredByOwnerAt.plusHours(4);
    // 다음 물주기 가능 시간이 현재 시간보다 이전이거나 같으면 true
    return !nextWaterableTime.isAfter(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
  }

  @Transient // DB에 저장하지 않는, 계산된 필드임을 명시
  public long getWaterableByOwnerInSeconds() {
    if (isWaterableByOwner()) {
      return 0L; // 이미 물주기가 가능하면 남은 시간은 0
    }
    LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    LocalDateTime nextWaterableTime = this.lastWateredByOwnerAt.plusHours(4);
    long remainingSeconds = Duration.between(now, nextWaterableTime).getSeconds();
    return Math.max(0L, remainingSeconds);
  }

  public void unlock() {
    this.isLocked = false;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public void increaseWaterCount() {
    this.waterCount++;
  }

  public void increaseSunlightCount() {
    this.sunlightCount++;
  }

  public void recordOwnerWateringTime() {
    this.lastWateredByOwnerAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
  }

  public void recordFriendWateringTime() {
    this.lastWateredByFriendAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
  }

  public void recordSunlightTime() {
    this.lastSunlightReceivedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
  }

  public void updateBackgroundImage(GardenBackground gardenBackground) {
    this.gardenBackground = gardenBackground;
  }

  // [추가] 정원에 배치된 아바타를 변경하는 메서드
  public void updateAvatar(Avatar avatar) {
    this.avatar = avatar;
  }

  public void setUser(User user) {
    this.user = user;
  }
}
