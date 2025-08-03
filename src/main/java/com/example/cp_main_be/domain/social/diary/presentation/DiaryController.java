package com.example.cp_main_be.domain.social.diary.presentation;

import com.example.cp_main_be.domain.social.diary.dto.response.DiaryResponse;
import com.example.cp_main_be.domain.social.diary.service.DiaryService;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.service.UserService;
import com.example.cp_main_be.global.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
public class DiaryController {

    private final DiaryService diaryService;
    private final UserService userService;

    @GetMapping("diaries")
    public ResponseEntity<ApiResponse<List<DiaryResponse>>> getDiaries() {
        String userUuid =
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userService.findUserByUuid(UUID.fromString(userUuid));
        List<DiaryResponse> diaryResponses = diaryService.findAllDiariesByUserId(user.getId());
        return ResponseEntity.ok(ApiResponse.success(diaryResponses));
    }
}
