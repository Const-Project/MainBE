package com.example.cp_main_be.domain.mission.keyword.service;

import com.example.cp_main_be.domain.mission.keyword.domain.repository.KeywordRepository;
import com.example.cp_main_be.domain.mission.keyword.dto.response.KeywordResponse;
import com.example.cp_main_be.domain.mission.keyword.dto.response.TodayKeywordResponse;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class KeywordService {

  private final KeywordRepository keywordRepository;

  public TodayKeywordResponse getTodayKeyword() {
    // 오늘의 키워드 등록에 따라 달라질 예정
    return new TodayKeywordResponse("today_keyword");
  }

  public List<KeywordResponse> getAllKeywords() {
    List<KeywordResponse> allKeywords =
        keywordRepository.findAll().stream()
            .map(keyword -> new KeywordResponse(keyword.getCreatedAt(), keyword.getKeyword()))
            .collect(Collectors.toList());

    return allKeywords;
  }
}
