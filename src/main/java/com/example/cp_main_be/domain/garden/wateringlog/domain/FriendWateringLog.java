package com.example.cp_main_be.domain.garden.wateringlog.domain;

import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.member.user.domain.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "friend_watering_log")
public class FriendWateringLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "water_giver_id", nullable = false)
  private User waterGiver;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "watered_garden_id", nullable = false)
  private Garden wateredGarden;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime wateredAt;

  @Builder
  public FriendWateringLog(User waterGiver, Garden wateredGarden) {
    this.waterGiver = waterGiver;
    this.wateredGarden = wateredGarden;
  }
}
