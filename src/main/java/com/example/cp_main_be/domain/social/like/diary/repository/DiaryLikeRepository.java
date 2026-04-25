package com.example.cp_main_be.domain.social.like.diary.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import com.example.cp_main_be.domain.social.like.diary.domain.DiaryLike;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryLikeRepository extends JpaRepository<DiaryLike, Long> {
  Optional<DiaryLike> findByUserAndDiary(User user, Diary diary);

  boolean existsByUserAndDiary(User user, Diary diary);

  long countByDiary(Diary diary);

  void deleteByUserAndDiary(User user, Diary diary);

  void deleteAllByUser(User user);

  @Modifying
  @Query("DELETE FROM DiaryLike dl WHERE dl.diary.user = :user")
  void deleteAllByDiaryUser(@Param("user") User user);

  // N+1 문제 해결을 위한 일괄 카운트 메서드
  @Query(
      "SELECT l.diary.id, COUNT(l.id) FROM DiaryLike l WHERE l.diary.id IN :diaryIds GROUP BY l.diary.id")
  List<Object[]> countByDiaryIds(@Param("diaryIds") List<Long> diaryIds);

  default Map<Long, Long> countLikesByDiaryIds(List<Long> diaryIds) {
    if (diaryIds == null || diaryIds.isEmpty()) {
      return Map.of();
    }
    return countByDiaryIds(diaryIds).stream()
        .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
  }
}
