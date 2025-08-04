package com.example.cp_main_be.domain.social.diary.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.social.diary.dto.request.DiaryWriteRequest;
import com.example.cp_main_be.domain.social.diary.dto.response.DiaryResponse;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(DiaryService.class) // 테스트할 서비스 클래스를 빈으로 등록
class DiaryServiceTest {

  @Autowired private DiaryService diaryService;

  @Autowired private DiaryRepository diaryRepository;

  @Autowired private UserRepository userRepository; // User 엔티티 저장을 위해 필요

  @DisplayName("특정 유저의 다이어리 목록 조회 성공")
  @Test
  void findAllDiariesByUserId_Success() {
    // given
    User user1 = userRepository.save(User.builder().username("testUser1").build());
    User user2 = userRepository.save(User.builder().username("testUser2").build());

    Diary diary1 =
        diaryRepository.save(
            Diary.builder().title("Diary 1").content("Content 1").user(user1).build());
    Diary diary2 =
        diaryRepository.save(
            Diary.builder().title("Diary 2").content("Content 2").user(user1).build());

    Diary diary3 =
        diaryRepository.save(
            Diary.builder().title("Diary 3").content("Content 3").user(user2).build());

    // when
    List<DiaryResponse> result = diaryService.findAllDiariesByUserId(user1.getId());

    // then
    assertThat(result).isNotNull();
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.get(0).getTitle()).isEqualTo(diary1.getTitle());
    assertThat(result.get(1).getTitle()).isEqualTo(diary2.getTitle());
  }

  @DisplayName("다이어리 등록 후 데이터베이스에 저장되었는지 확인")
  @Test
  void registerDiary_and_verify_saved_in_db() {
    // given
    User user = userRepository.save(User.builder().username("testUser").build());

    String title = "Test Title";
    String content = "Test Content";
    String imageUrl = "http://test.com/image.png";
    String keyword = "testKeyword";
    Boolean isPublic = true;

    // when
    Long newDiaryId = diaryService.registerDiary(title, content, imageUrl, keyword, user, isPublic);

    // then
    // 반환된 ID를 사용해 데이터베이스에서 다이어리 객체를 조회
    Optional<Diary> foundDiary = diaryRepository.findById(newDiaryId);

    // 조회된 객체가 존재하는지, 필드 값이 예상과 일치하는지 확인
    assertThat(foundDiary).isPresent();
    assertThat(foundDiary.get().getTitle()).isEqualTo(title);
    assertThat(foundDiary.get().getContent()).isEqualTo(content);
    assertThat(foundDiary.get().getUser().getId()).isEqualTo(user.getId());
  }

  @DisplayName("특정 다이어리 조회 성공 by id")
  @Test
  void getDiaryById_success() throws Exception {
    // given
    User user1 = userRepository.save(User.builder().username("testUser1").build());
    User user2 = userRepository.save(User.builder().username("testUser2").build());

    Diary diary1 =
        diaryRepository.save(
            Diary.builder().title("Diary 1").content("Content 1").user(user1).build());
    Diary diary2 =
        diaryRepository.save(
            Diary.builder().title("Diary 2").content("Content 2").user(user2).build());

    // when
    DiaryResponse diary1ById = diaryService.getDiaryResponseById(diary1.getId());
    DiaryResponse diary2ById = diaryService.getDiaryResponseById(diary2.getId());

    // then
    assertThat(diary1ById).isNotNull();
    assertThat(diary1ById.getId()).isEqualTo(diary1.getId());
    assertThat(diary1ById.getTitle()).isEqualTo(diary1.getTitle());
    assertThat(diary1ById.getContent()).isEqualTo(diary1.getContent());

    assertThat(diary2ById).isNotNull();
    assertThat(diary2ById.getId()).isEqualTo(diary2.getId());
    assertThat(diary2ById.getTitle()).isEqualTo(diary2.getTitle());
    assertThat(diary2ById.getContent()).isEqualTo(diary2.getContent());
  }

  @DisplayName("다이어리 수정 성공")
  @Test
  void update_diary_success() throws Exception {
    // given
    User user1 = userRepository.save(User.builder().username("testUser1").build());
    Diary firstDiary = Diary.builder().title("first 1").content("first 1").user(user1).build();
    diaryRepository.save(firstDiary);

    String title = "Test Title";
    String content = "Test Content";
    String imageUrl = "http://test.com/image.png";
    String keyword = "testKeyword";
    Boolean isPublic = true;

    DiaryWriteRequest diaryWriteRequest =
        DiaryWriteRequest.builder()
            .title(title)
            .content(content)
            .imageUrl(imageUrl)
            .keyword(keyword)
            .isPublic(isPublic)
            .build();

    // when
    Diary diary = diaryService.updateDiary(firstDiary.getId(), diaryWriteRequest);

    // then
    assertThat(diary.getId()).isEqualTo(firstDiary.getId());
    assertThat(diary.getTitle()).isEqualTo(title);
    assertThat(diary.getContent()).isEqualTo(content);
    assertThat(diary.getImageUrl()).isEqualTo(imageUrl);
  }
}
