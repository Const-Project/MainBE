package com.example.cp_main_be.domain.social.diaryimage.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryImageRepository
    extends JpaRepository<
        com.example.cp_main_be.domain.social.diaryimage.domain.DiaryImage, Long> {}
