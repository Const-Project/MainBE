package com.example.cp_main_be.avatar.domain.repository;

import com.example.cp_main_be.avatar.domain.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {

    List<Avatar> findByIsDefaultAvatarTrue(); // 기본 아바타 조회
    List<Avatar> findByUserIdAndIsDefaultAvatarFalse(Long userId); // 특정 유저가 소유한 아바타 조회
}
