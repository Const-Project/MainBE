package com.example.cp_main_be.domain.social.avatarpost.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvatarPostRepository extends JpaRepository<AvatarPost, Long> {
  List<AvatarPost> findByUserIn(List<User> users, Pageable pageable);
}
