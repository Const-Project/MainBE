package com.example.cp_main_be.domain.mission.wishTree;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
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
    WishTree wishTree = findOrCreateWishTree(userId);

    // [추가] 나무 또는 최종 단계에 도달하면 더 이상 포인트를 추가하지 않음
    if (wishTree.getStage() == WishTreeStage.TREE || wishTree.getStage() == WishTreeStage.FINAL) {
      return wishTree; // 아무 작업도 하지 않고 현재 상태 반환
    }

    wishTree.addPoints(points);
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
