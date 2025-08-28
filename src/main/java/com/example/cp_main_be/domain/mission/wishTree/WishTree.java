package com.example.cp_main_be.domain.mission.wishTree;

import com.example.cp_main_be.domain.member.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WishTree {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private Long points;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WishTreeStage stage;

  @Builder
  public WishTree(User user) {
    this.user = user;
    this.points = user.getExperience();
    this.stage = WishTreeStage.SPROUT;
  }

  @Column(nullable = false)
  private boolean isUnlockable = false;

  /**
   * 포인트 추가 및 성장 로직
   *
   * @param amount 추가할 포인트
   * @return 성장을 했는지 여부
   */
  public void addPoints(Long points) {
    this.points += points;

    // 이미 해금 가능 상태이거나, 다음 스테이지가 없으면 아무것도 하지 않음
    if (this.isUnlockable || this.stage.getNextStage() == null) {
      return;
    }

    // 다음 스테이지의 요구 포인트를 넘었는지 확인
    WishTreeStage nextStage = this.stage.getNextStage();
    if (this.points >= this.stage.getRequiredPointsForNextStage()) {
      this.isUnlockable = true; // 👈 Stage를 바로 바꾸는 대신, 해금 가능 상태로 변경
    }
  }

  public void evolveStage() {
    if (!this.isUnlockable) {
      // 해금 불가능한 상태에서 호출 시 예외 처리 또는 로깅
      return;
    }

    WishTreeStage nextStage = this.stage.getNextStage();
    if (nextStage != null) {
      this.stage = nextStage;
      this.isUnlockable = false; // 상태 플래그 초기화
    }
  }
}
