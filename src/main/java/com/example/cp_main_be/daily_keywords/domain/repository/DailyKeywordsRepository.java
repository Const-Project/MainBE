package com.example.cp_main_be.daily_keywords.domain.repository;

import com.example.cp_main_be.daily_keywords.domain.DailyKeywords;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyKeywordsRepository extends JpaRepository<DailyKeywords, Long> {}
