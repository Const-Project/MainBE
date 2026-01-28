package com.example.cp_main_be.domain.member.log.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import jakarta.persistence.*;
import java.time.LocalDate;
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
@Table(
    name = "user_daily_activity_log",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_user_date",
          columnNames = {"user_id", "date"})
    })
public class UserDailyActivityLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private LocalDate date;

  @Column(nullable = false)
  private boolean hasWatered;

  @Column(nullable = false)
  private boolean hasSunlight;

  @CreatedDate
  @Column(updatable = false)
  private LocalDate createdAt;

  @Builder
  public UserDailyActivityLog(User user, LocalDate date, boolean hasWatered, boolean hasSunlight) {
    this.user = user;
    this.date = date;
    this.hasWatered = hasWatered;
    this.hasSunlight = hasSunlight;
  }

  public void updateWatered(boolean hasWatered) {
    this.hasWatered = hasWatered;
  }

  public void updateSunlight(boolean hasSunlight) {
    this.hasSunlight = hasSunlight;
  }
}
