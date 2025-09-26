package com.example.cp_main_be.domain.social.like.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.like.domain.Like;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Like, Long> {
  Optional<Like> findByUserAndTargetIdAndTargetType(User user, Long targetId, String targetType);

  boolean existsByUserAndTargetIdAndTargetType(User user, Long targetId, String targetType);

  boolean existsByUserIdAndTargetId(Long id, Long postId);

  boolean existsByUserIdAndTargetIdAndTargetType(Long id, Long postId, String post);

  //  long countByAvatarPost(AvatarPost post);

  //  Optional<Like> findByUserAndAvatarPost(User currentUser, AvatarPost post);

  long countByTargetIdAndTargetType(Long targetId, String targetType);

  // [추가] 여러 targetId에 대한 좋아요 수를 한 번의 쿼리로 조회
  @Query(
      "SELECT l.targetId, COUNT(l.id) FROM Like l WHERE l.targetType = :targetType AND l.targetId IN :targetIds GROUP BY l.targetId")
  List<Object[]> countByTargetIdsAndTargetType(
      @Param("targetIds") List<Long> targetIds, @Param("targetType") String targetType);

  // [추가] 위 메서드를 편리하게 사용하기 위한 default 메서드
  default Map<Long, Long> countLikesByTargetIds(List<Long> targetIds, String targetType) {
    return countByTargetIdsAndTargetType(targetIds, targetType).stream()
        .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
  }
}
