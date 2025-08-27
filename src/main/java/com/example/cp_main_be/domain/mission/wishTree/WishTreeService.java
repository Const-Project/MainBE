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
    // 1. 유저의 소망 나무를 찾거나, 없으면 새로 생성
    WishTree wishTree = findOrCreateWishTree(userId);

    // 2. WishTree 엔티티에 포인트 추가 및 해금 가능 상태로 변경 (내부 로직)
    wishTree.addPoints(points);

    // 3. Stage 성장 및 이벤트 발행 로직 모두 제거
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
