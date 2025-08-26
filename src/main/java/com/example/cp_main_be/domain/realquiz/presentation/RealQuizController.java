package com.example.cp_main_be.domain.realquiz.presentation;


import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.quiz.enums.QuizType;
import com.example.cp_main_be.domain.realquiz.dto.request.RealQuizAnswerRequestDTO;
import com.example.cp_main_be.domain.realquiz.dto.request.RealQuizCreateRequestDTO;
import com.example.cp_main_be.domain.realquiz.dto.request.UserQuizCreateRequestDTO;
import com.example.cp_main_be.domain.realquiz.dto.response.RealQuizAnswerResponseDTO;
import com.example.cp_main_be.domain.realquiz.dto.response.RealQuizResponseDTO;
import com.example.cp_main_be.domain.realquiz.dto.response.UserQuizCreateResponseDTO;
import com.example.cp_main_be.domain.realquiz.service.RealQuizService;
import com.example.cp_main_be.global.common.ApiResponse;
import com.google.protobuf.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/realQuiz")
@RequiredArgsConstructor
@Tag(name = "퀴즈 컨트롤러", description = "퀴즈 관련 기능을 제공합니다.")
public class RealQuizController {

    private final RealQuizService realQuizService;

    @PostMapping("/create")
    @Operation(summary = "퀴즈 생성 API")
    public ApiResponse<RealQuizResponseDTO> createRealQuiz(@RequestBody RealQuizCreateRequestDTO requestDTO) {
        RealQuizResponseDTO responseDTO = realQuizService.createRealQuiz(requestDTO);
        return ApiResponse.success(responseDTO);
    }

    @PostMapping("/assign")
    @Operation(summary = "유저에게 퀴즈 수동 할당 API")
    public ApiResponse<UserQuizCreateResponseDTO> createUserQuiz(@RequestBody UserQuizCreateRequestDTO requestDTO) {
        UserQuizCreateResponseDTO responseDTO = realQuizService.createUserQuiz(requestDTO.getUserId(), requestDTO.getQuizId());
        return ApiResponse.success(responseDTO);
    }

    @GetMapping("/list")
    @Operation(summary = "자신의 퀴즈 목록 조회 API")
    public ApiResponse<RealQuizResponseDTO> getRealQuiz(@AuthenticationPrincipal User user, @RequestParam QuizType quizType) {
        RealQuizResponseDTO responseDTO = realQuizService.getRealQuiz(user,quizType);
        return ApiResponse.success(responseDTO);
    }

    @GetMapping("/{quizId}/answer")
    @Operation(summary = "퀴즈 정답 제출 API")
    public ApiResponse<RealQuizAnswerResponseDTO> getRealQuizAnswer(@PathVariable Long quizId, @RequestBody RealQuizAnswerRequestDTO requestDTO)
    {
        RealQuizAnswerResponseDTO responseDTO = realQuizService.getRealQuizAnswer(quizId, requestDTO);
        return ApiResponse.success(responseDTO);
    }
}
