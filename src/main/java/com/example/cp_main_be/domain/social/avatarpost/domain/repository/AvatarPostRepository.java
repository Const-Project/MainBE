package com.example.cp_main_be.domain.social.avatarpost.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import java.util.List;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AvatarPostRepository extends JpaRepository<AvatarPost, Long> {
  List<AvatarPost> findByUserIn(List<User> users, Pageable pageable);
}
public interface AvatarPostRepository extends JpaRepository<AvatarPost, Long> {

  @Query("SELECT ap FROM AvatarPost ap LEFT JOIN FETCH ap.comments WHERE ap.id = :postId")
  Optional<AvatarPost> findByIdWithComments(Long postId);
}
