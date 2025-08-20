package com.example.cp_main_be.domain.garden.domain;

import com.example.cp_main_be.domain.user.domain.User;
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
  private User user;

  @Column(nullable = false)
  private Integer slotNumber;

  @Column(nullable = false)
  private Integer waterCount = 0;

  @Column(nullable = false)
  private Integer sunlightCount = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "garden_background_id")
  private GardenBackground gardenBackground;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate private LocalDateTime updatedAt;

  @Builder
  public Garden(User user, Integer slotNumber, GardenBackground gardenBackground) {
    this.user = user;
    this.slotNumber = slotNumber;
    this.waterCount = 0;
    this.sunlightCount = 0;
    this.gardenBackground = gardenBackground;
  }

  public void increaseWaterCount() {
    this.waterCount++;
  }

  public void increaseSunlightCount() {
    this.sunlightCount++;
  }

  public void updateBackgroundImage(GardenBackground gardenBackground) {
    this.gardenBackground = gardenBackground;
  }

  public void increaseWaterCount() {
    this.waterCount++;
  }

  public void increaseSunlightCount() {
    this.sunlightCount++;
  }
}
