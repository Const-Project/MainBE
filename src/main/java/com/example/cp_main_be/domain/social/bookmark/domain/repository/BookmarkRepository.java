package com.example.cp_main_be.domain.social.bookmark.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.bookmark.domain.Bookmark;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

  Optional<Bookmark> findByUserAndAvatarPost(User user, AvatarPost avatarPost);

  boolean existsByUserAndAvatarPost(User user, AvatarPost avatarPost);

  List<Bookmark> findAllByUserOrderByCreatedAtDesc(User user);
}
