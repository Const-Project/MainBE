package com.example.cp_main_be.domain.realquiz.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.realquiz.UserQuiz;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserQuizRepository extends JpaRepository<UserQuiz, Long> {
  Optional<UserQuiz> findByUser(User user);
}
