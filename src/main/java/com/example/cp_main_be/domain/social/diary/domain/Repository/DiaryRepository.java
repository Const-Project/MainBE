package com.example.cp_main_be.domain.social.diary.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findByUserInAndIsPublicIsTrue(List<User> users, Pageable pageable);

    List<Diary> findByIsPublicIsTrue(Pageable pageable);

    List<Diary> findByUserOrderByCreatedAtDesc(User user);

}
