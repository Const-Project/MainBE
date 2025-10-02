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
    // [수정] User에 더 이상 experience 필드가 없으므로, 신규 생성 시 0점으로 시작합니다.
    this.points = 0L;
    this.stage = WishTreeStage.SPROUT;
  }

  public void addPoints(Long points) {
    this.points += points;
    evolveStageIfNeeded();
  }

  /**
   * @deprecated canEvolve() 메서드 사용을 권장합니다.
   * @return 진화 가능 여부
   */
  @Deprecated
  public boolean isUnlockable() {
    // [수정] 중복 로직을 제거하고 canEvolve()를 사용하도록 통일합니다.
    return canEvolve();
  }

  public boolean canEvolve() {
    // 다음 스테이지가 없으면 진화 불가
    if (this.stage == WishTreeStage.FINAL) {
      return false;
    }
    return this.stage.getRequiredPointsForNextStage() <= this.points;
  }

  private void evolveStageIfNeeded() {
    // 진화할 수 있는 동안 계속 반복 (경험치를 몰아서 얻었을 경우 대비)
    while (canEvolve()) {
      this.stage = this.stage.getNextStage();
      // 연관된 User 객체에 해금 가능 횟수를 1 늘려달라고 요청
      this.user.incrementUnlockableGardenCount();
    }
  }
}
