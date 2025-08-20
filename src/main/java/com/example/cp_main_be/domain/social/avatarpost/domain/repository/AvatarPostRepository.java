package com.example.cp_main_be.domain.social.avatarpost.domain.repository;

import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AvatarPostRepository extends JpaRepository<AvatarPost, Long> {

  @Query("SELECT ap FROM AvatarPost ap LEFT JOIN FETCH ap.comments WHERE ap.id = :postId")
  Optional<AvatarPost> findByIdWithComments(Long postId);
}
