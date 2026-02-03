package com.example.cp_main_be.domain.member.userblock;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.social.follow.domain.repository.FollowRepository;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockService {
  private final UserBlockRepository userBlockRepository;
  private final UserRepository userRepository;
  private final FollowRepository followRepository;

  public void blockUser(User blockerUser, Long userIdToBlock) {
    User blockedUser =
        userRepository
            .findById(userIdToBlock)
            .orElseThrow(() -> new UserNotFoundException("차단할 사용자를 찾을 수 없습니다."));

    if (userBlockRepository.existsByBlockerUserAndBlockedUser(blockerUser, blockedUser)) {
      throw new IllegalStateException("이미 차단한 사용자입니다.");
    }

    // 차단 시 상호 팔로우 관계 제거
    followRepository.deleteByFollowerAndFollowing(blockerUser, blockedUser);
    followRepository.deleteByFollowerAndFollowing(blockedUser, blockerUser);

    UserBlock userBlock =
        UserBlock.builder().blockerUser(blockerUser).blockedUser(blockedUser).build();
    userBlockRepository.save(userBlock);
  }

  public void unblockUser(User blockerUser, Long userIdToUnblock) {
    User blockedUser =
        userRepository
            .findById(userIdToUnblock)
            .orElseThrow(() -> new UserNotFoundException("차단 해제할 사용자를 찾을 수 없습니다."));

    if (!userBlockRepository.existsByBlockerUserAndBlockedUser(blockerUser, blockedUser)) {
      throw new IllegalStateException("차단 관계가 존재하지 않습니다.");
    }

    userBlockRepository.deleteByBlockerUserAndBlockedUser(blockerUser, blockedUser);
  }

  public boolean isBlocked(User blockerUser, User blockedUser) {
    return userBlockRepository.existsByBlockerUserAndBlockedUser(blockerUser, blockedUser);
  }
}
