package com.example.cp_main_be.domain.garden.garden.domain;

import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
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

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate private LocalDateTime updatedAt;

  @Builder
  public Garden(User user, Integer slotNumber, GardenBackground gardenBackground, Avatar avatar) {
    this.user = user;
    this.slotNumber = slotNumber;
    this.waterCount = 0;
    this.sunlightCount = 0;
    this.gardenBackground = gardenBackground;
    this.avatar = avatar; // [추가]
  }

  public void increaseWaterCount() {
    this.waterCount++;
  }

  public void increaseSunlightCount() {
    this.sunlightCount++;
  }

  public void recordOwnerWateringTime() {
    this.lastWateredByOwnerAt = LocalDateTime.now();
  }

  public void recordFriendWateringTime() {
    this.lastWateredByFriendAt = LocalDateTime.now();
  }

  public void updateBackgroundImage(GardenBackground gardenBackground) {
    this.gardenBackground = gardenBackground;
  }

  // [추가] 정원에 배치된 아바타를 변경하는 메서드
  public void updateAvatar(Avatar avatar) {
    this.avatar = avatar;
  }
}
