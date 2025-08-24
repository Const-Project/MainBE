package com.example.cp_main_be.domain.social.comment.domain.repository;

import com.example.cp_main_be.domain.social.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
