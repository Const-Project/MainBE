package com.example.cp_main_be.domain.tracking.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "tracking_report_view",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_tracking_report_view_user_cycle_key",
          columnNames = {"user_id", "cycle_key"})
    })
public class TrackingReportView {

  private static final int CYCLE_KEY_MAX_LENGTH = 32;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 한글 주석:
  // 같은 사용자가 같은 14일 주기를 중복 확인하더라도 한 번만 저장되도록 user + cycleKey 를 묶어 관리한다.
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "cycle_key", nullable = false, length = CYCLE_KEY_MAX_LENGTH)
  private String cycleKey;

  @Column(name = "viewed_at", nullable = false)
  private LocalDateTime viewedAt;

  @Builder
  public TrackingReportView(User user, String cycleKey, LocalDateTime viewedAt) {
    this.user = user;
    this.cycleKey = cycleKey;
    this.viewedAt = viewedAt;
  }
}
