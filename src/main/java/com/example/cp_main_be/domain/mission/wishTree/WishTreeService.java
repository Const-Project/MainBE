package com.example.cp_main_be.domain.mission.wishTree;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.event.WishTreeEvolvedEvent;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishTreeService {

  private final WishTreeRepository wishTreeRepository;
  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;

  // private final NotificationService notificationService; // 알림 발송을 위해 의존

  @Transactional
  public WishTree addPointsToWishTree(Long userId, Long points) {
    // 1. 유저의 소망 나무를 찾거나, 없으면 새로 생성
    WishTree wishTree = findOrCreateWishTree(userId);

    // 2. WishTree 엔티티에 포인트 추가 및 성장 여부 확인
    boolean hasEvolved = wishTree.addPoints(points);

    // 3. 만약 나무가 성장했다면, 추가 로직 실행
    if (hasEvolved) {
      // 4. 만약 '나무' 단계로 성장했다면, 새로운 Garden 해금
      if (wishTree.getStage() == WishTreeStage.TREE) {
        eventPublisher.publishEvent(new WishTreeEvolvedEvent(userId));
        // notificationService.send(userId, "새로운 텃밭이 열렸어요!");
      }
    }
    return wishTree;
  }

  public WishTree findOrCreateWishTree(Long userId) {
    return wishTreeRepository
        .findByUserId(userId)
        .orElseGet(
            () -> {
              User user =
                  userRepository
                      .findById(userId)
                      .orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저입니다."));
              return wishTreeRepository.save(new WishTree(user));
            });
  }
}
