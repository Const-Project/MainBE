package com.example.cp_main_be.domain.member.level.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.global.event.LevelUpEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LevelService {

  private static final int BASE_EXP = 1000;
  private static final int EXP_INCREMENT = 150;

  private final ApplicationEventPublisher eventPublisher;

  public void checkLevelUp(User user) {
    long currentLevel = user.getLevel();
    long currentExp = user.getExperience();

    long requiredExp = calculateRequiredExp(currentLevel);

    while (currentExp >= requiredExp) {
      // 2. 실제 레벨업 처리는 User 객체에 위임
      long remainingExp = currentExp - requiredExp;
      user.levelUp(remainingExp);

      // 3. 레벨업 이벤트 발행
      eventPublisher.publishEvent(new LevelUpEvent(user, user.getLevel()));

      // 다음 레벨업 체크를 위해 값 업데이트
      currentLevel = user.getLevel();
      currentExp = user.getExperience();
      requiredExp = calculateRequiredExp(currentLevel);
    }
  }

  // 경험치를 추가하고, 레벨업이 필요한지 확인하는 메서드
  public void addExperienceAndCheckLevelUp(User user, int expToAdd) {
    user.addExperience(expToAdd);
    checkLevelUp(user);
  }

  private long calculateRequiredExp(long level) {
    // 레벨 1 -> 2 필요 경험치: 1150
    // 레벨 2 -> 3 필요 경험치: 1300
    // ...
    return BASE_EXP + (EXP_INCREMENT * level);
  }
}
