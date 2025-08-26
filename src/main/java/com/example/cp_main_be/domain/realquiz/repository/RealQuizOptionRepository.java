package com.example.cp_main_be.domain.realquiz.repository;

import com.example.cp_main_be.domain.realquiz.RealQuiz;
import com.example.cp_main_be.domain.realquiz.RealQuizOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RealQuizOptionRepository extends JpaRepository<RealQuizOption, Long> {
    List<RealQuizOption> findAllByRealQuiz(RealQuiz realQuiz);
}
