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

  /**
   * 포인트 추가 및 성장 로직
   *
   * @param amount 추가할 포인트
   * @return 성장을 했는지 여부
   */
  public boolean addPoints(Long amount) {
    WishTreeStage previousStage = this.stage;
    this.points += amount;
    WishTreeStage newStage = WishTreeStage.getStageForPoints(this.points);

    if (newStage != previousStage) {
      this.stage = newStage;
      return true; // 성장했다!
    }
    return false; // 성장 안함
  }
}
