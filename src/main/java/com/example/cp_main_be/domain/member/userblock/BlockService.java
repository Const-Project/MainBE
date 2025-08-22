package com.example.cp_main_be.domain.member.userblock;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
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

  public void blockUser(User blockerUser, Long userIdToBlock) {
    User blockedUser =
        userRepository
            .findById(userIdToBlock)
            .orElseThrow(() -> new UserNotFoundException("차단할 사용자를 찾을 수 없습니다."));

    // TODO: 이미 차단했는지, 자기 자신을 차단하는지 등 예외 처리

    UserBlock userBlock =
        UserBlock.builder().blockerUser(blockerUser).blockedUser(blockedUser).build();
    userBlockRepository.save(userBlock);
  }
}
