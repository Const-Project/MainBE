package com.example.cp_main_be.domain.social.diary.service;

import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.social.diary.dto.response.DiaryResponse;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(DiaryService.class) // 테스트할 서비스 클래스를 빈으로 등록
class DiaryServiceTest {

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private UserRepository userRepository; // User 엔티티 저장을 위해 필요

    @DisplayName("특정 유저의 다이어리 목록 조회 성공")
    @Test
    void findAllDiariesByUserId_Success() {
        // given
        // 1. 테스트 유저 저장
        User user1 = userRepository.save(User.builder().username("testUser1").build());
        User user2 = userRepository.save(User.builder().username("testUser2").build());

        // 2. user1의 다이어리 2개 저장
        Diary diary1 = diaryRepository.save(
                Diary.builder().title("Diary 1").content("Content 1").user(user1).build()
        );
        Diary diary2 = diaryRepository.save(
                Diary.builder().title("Diary 2").content("Content 2").user(user1).build()
        );

        // 3. user2의 다이어리 1개 저장
        Diary diary3 = diaryRepository.save(
                Diary.builder().title("Diary 3").content("Content 3").user(user2).build()
        );

        // when
        // user1의 다이어리 목록을 조회
        List<DiaryResponse> result = diaryService.findAllDiariesByUserId(user1.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getTitle()).isEqualTo(diary1.getTitle());
        assertThat(result.get(1).getTitle()).isEqualTo(diary2.getTitle());
    }
}