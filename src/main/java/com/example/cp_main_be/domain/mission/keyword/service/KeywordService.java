package com.example.cp_main_be.domain.mission.keyword.service;

import com.example.cp_main_be.domain.mission.keyword.domain.Keyword;
import com.example.cp_main_be.domain.mission.keyword.domain.repository.KeywordRepository;
import com.example.cp_main_be.domain.mission.keyword.dto.request.AddKeywordRequest;
import com.example.cp_main_be.domain.mission.keyword.dto.response.KeywordResponse;
import com.example.cp_main_be.domain.mission.keyword.dto.response.TodayKeywordResponse;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class KeywordService {

  private final KeywordRepository keywordRepository;

  public void addNewKeyword(AddKeywordRequest request) {
    Keyword newKeyword = Keyword.builder().keyword(request.getKeyword()).build();

    keywordRepository.save(newKeyword); // save 메소드 호출
  }

  public TodayKeywordResponse getTodayKeyword() {
    // 1. DB에 저장된 전체 키워드 개수를 가져옵니다.
    long totalKeywords = keywordRepository.count();

    // 키워드가 하나도 없으면 기본 메시지를 반환합니다.
    if (totalKeywords == 0) {
      return new TodayKeywordResponse("등록된 키워드가 없습니다.");
    }

    // 2. 기준이 될 시작 날짜를 정합니다. (서비스 시작일 등)
    LocalDate startDate = LocalDate.of(2025, 10, 1);
    LocalDate today = LocalDate.now();

    // 3. 시작 날짜로부터 오늘까지 며칠이 지났는지 계산합니다.
    long daysSinceStart = ChronoUnit.DAYS.between(startDate, today);

    // 4. (지난 날짜 % 전체 키워드 개수)로 오늘 보여줄 키워드의 순번(index)을 구합니다.
    int keywordIndex = (int) (daysSinceStart % totalKeywords);

    // 5. PageRequest를 이용해 해당 순번의 키워드 하나만 조회합니다. (효율적)
    Page<Keyword> keywordPage = keywordRepository.findAll(PageRequest.of(keywordIndex, 1));

    if (keywordPage.hasContent()) {
      String todayKeyword = keywordPage.getContent().get(0).getKeyword();
      return new TodayKeywordResponse(todayKeyword);
    }

    // 위에서 개수 체크를 했기 때문에 이 경우는 거의 발생하지 않지만, 만약을 대비한 코드입니다.
    return new TodayKeywordResponse("키워드를 가져오는데 실패했습니다.");
  }

  public List<KeywordResponse> getAllKeywords() {

    return keywordRepository.findAll().stream()
        .map(keyword -> new KeywordResponse(keyword.getCreatedAt(), keyword.getKeyword()))
        .collect(Collectors.toList());
  }
}
