package com.example.cp_main_be.domain.realquiz.repository;

import com.example.cp_main_be.domain.realquiz.RealQuiz;
import com.example.cp_main_be.domain.realquiz.RealQuizOption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealQuizOptionRepository extends JpaRepository<RealQuizOption, Long> {
  List<RealQuizOption> findAllByRealQuiz(RealQuiz realQuiz);
}
