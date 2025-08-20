package com.example.cp_main_be.domain.content.avatar.domain.repository;

import com.example.cp_main_be.domain.content.avatar.domain.Avatar;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {

  List<Avatar> findByIsDefaultAvatarTrue(); // 기본 아바타 조회

  List<Avatar> findByUserIdAndIsDefaultAvatarFalse(Long userId); // 특정 유저가 소유한 아바타 조회
}
