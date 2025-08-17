package com.example.cp_main_be.domain.keyword.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.cp_main_be.domain.keyword.domain.Keyword;
import com.example.cp_main_be.domain.keyword.domain.repository.KeywordRepository;
import com.example.cp_main_be.domain.keyword.dto.response.KeywordResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;

@ExtendWith(MockitoExtension.class)
@Import(KeywordService.class)
class KeywordServiceTest {

  @InjectMocks private KeywordService keywordService;

  @Mock private KeywordRepository keywordRepository;

  @DisplayName("전체 키워드 목록 조회 서비스 테스트")
  @Test
  void getAllKeywords_success() throws Exception {
    // given
    List<Keyword> mockKeywords =
        List.of(
            Keyword.builder().id(1L).keyword("keyword1").build(),
            Keyword.builder().id(2L).keyword("keyword2").build());
    given(keywordRepository.findAll()).willReturn(mockKeywords);

    // when
    List<KeywordResponse> allKeywords = keywordService.getAllKeywords();

    // then
    assertThat(allKeywords).hasSize(2);
    assertThat(allKeywords.get(0).getKeyword()).isEqualTo("keyword1");
    assertThat(allKeywords.get(1).getKeyword()).isEqualTo("keyword2");
    then(keywordRepository).should().findAll();
  }
}
