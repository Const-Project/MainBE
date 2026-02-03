package com.example.cp_main_be.domain.member.userblock;

import com.example.cp_main_be.domain.member.user.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {
  // 현재 사용자가 차단한 모든 사용자의 ID 목록을 조회
  @Query("SELECT ub.blockedUser.id FROM UserBlock ub WHERE ub.blockerUser = :blocker")
  List<Long> findBlockedUserIdsByBlocker(@Param("blocker") User blocker);

  boolean existsByBlockerUserAndBlockedUser(User blockerUser, User blockedUser);

  void deleteByBlockerUserAndBlockedUser(User blockerUser, User blockedUser);
}
