package com.example.cp_main_be.domain.social.diary.service;

import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.social.diary.dto.response.DiaryResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryService {

    private final DiaryRepository diaryRepository;

    public List<DiaryResponse> findAllDiariesByUserId(Long userId) {
        List<Diary> diaries = diaryRepository.findAllByUser_id(userId);
        return diaries.stream()
                .map(DiaryResponse::from)
                .collect(Collectors.toList());
    }
}
