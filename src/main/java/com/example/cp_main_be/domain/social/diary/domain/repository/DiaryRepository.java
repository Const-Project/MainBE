package com.example.cp_main_be.domain.social.diary.domain.repository;

import com.example.cp_main_be.domain.social.diary.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Integer> {

  List<Diary> findAllByUserId(Long userId);

  Diary findDiaryById(Long id);

  Optional<Diary> findById(Long id);

  void deleteById(Long id);
}
