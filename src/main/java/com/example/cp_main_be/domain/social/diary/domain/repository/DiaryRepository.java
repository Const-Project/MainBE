package com.example.cp_main_be.domain.social.diary.domain.repository;

import com.example.cp_main_be.domain.social.diary.domain.Diary;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Integer> {

  List<Diary> findAllByUser_id(Long userId);

  Diary findDiaryById(Long id);

  Optional<Diary> findById(Long id);
}
