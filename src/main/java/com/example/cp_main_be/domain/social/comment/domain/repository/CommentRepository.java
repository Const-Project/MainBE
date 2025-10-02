package com.example.cp_main_be.domain.social.comment.domain.repository;

import com.example.cp_main_be.domain.social.comment.domain.Comment;
import com.example.cp_main_be.domain.social.comment.dto.CommentCountDto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  // 1. AvatarPost ID 목록으로 댓글 수 조회
  @Query(
      "SELECT new com.example.cp_main_be.domain.social.comment.dto.CommentCountDto(c.avatarPost.id, COUNT(c.id)) "
          + "FROM Comment c "
          + "WHERE c.avatarPost.id IN :avatarPostIds "
          + "GROUP BY c.avatarPost.id")
  List<CommentCountDto> countCommentsByAvatarPostIds(
      @Param("avatarPostIds") List<Long> avatarPostIds);

  // 2. Diary ID 목록으로 댓글 수 조회
  @Query(
      "SELECT new com.example.cp_main_be.domain.social.comment.dto.CommentCountDto(c.diary.id, COUNT(c.id)) "
          + "FROM Comment c "
          + "WHERE c.diary.id IN :diaryIds "
          + "GROUP BY c.diary.id")
  List<CommentCountDto> countCommentsByDiaryIds(@Param("diaryIds") List<Long> diaryIds);
}
