package com.example.cp_main_be.domain.mission.wishTree;

import com.example.cp_main_be.domain.garden.garden.service.GardenService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishTreeService {

  private final WishTreeRepository wishTreeRepository;
  private final GardenService gardenService; // Garden 해금을 위해 의존
  private final UserRepository userRepository;

  // private final NotificationService notificationService; // 알림 발송을 위해 의존

  @Transactional
  public void addPointsToWishTree(Long userId, int points) {
    // 1. 유저의 소망 나무를 찾거나, 없으면 새로 생성
    WishTree wishTree =
        wishTreeRepository
            .findByUserId(userId)
            .orElseGet(
                () -> {
                  // TODO: User 객체를 찾는 로직 필요
                  User user = userRepository.findById(userId).orElseThrow();
                  return wishTreeRepository.save(new WishTree(user));
                });

    // 2. WishTree 엔티티에 포인트 추가 및 성장 여부 확인
    boolean hasEvolved = wishTree.addPoints(points);

    // 3. 만약 나무가 성장했다면, 추가 로직 실행
    if (hasEvolved) {
      // notificationService.send(userId, "소망 나무가 " + wishTree.getStage().getKoreanName() + "으로
      // 자랐어요!");

      // 4. 만약 '나무' 단계로 성장했다면, 새로운 Garden 해금
      if (wishTree.getStage() == WishTreeStage.TREE) {
        gardenService.unlockNewGardenSlot(userId);
        // notificationService.send(userId, "새로운 텃밭이 열렸어요!");
      }
    }
  }
}
