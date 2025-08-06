package com.example.cp_main_be.domain.keyword.service;

import com.example.cp_main_be.domain.keyword.domain.repository.KeywordRepository;
import com.example.cp_main_be.domain.keyword.dto.response.KeywordResponse;
import com.example.cp_main_be.domain.keyword.dto.response.TodayKeywordResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
