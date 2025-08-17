package com.example.cp_main_be.domain.social.diaryimage.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryImageRepository extends JpaRepository<DiaryImage, Integer> {

  Optional<DiaryImage> findById(Long imageId);

  void deleteById(Long id);
}
