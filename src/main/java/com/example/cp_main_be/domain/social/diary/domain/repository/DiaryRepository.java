package com.example.cp_main_be.domain.social.diary.domain.repository;

import com.example.cp_main_be.domain.social.diary.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary,Integer> {

    List<Diary> findAllByUser_id(Long userId);
}
