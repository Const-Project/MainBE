package com.example.cp_main_be.domain.social.bookmark.domain.repository;

import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.bookmark.domain.Bookmark;
import com.example.cp_main_be.domain.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

  Optional<Bookmark> findByUserAndAvatarPost(User user, AvatarPost avatarPost);
}
