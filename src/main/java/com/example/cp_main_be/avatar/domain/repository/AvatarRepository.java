package com.example.cp_main_be.avatar.domain.repository;

import com.example.cp_main_be.avatar.domain.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {
}
