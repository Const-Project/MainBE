package com.example.cp_main_be.domain.user_daily_missions.presentation;

import com.example.cp_main_be.domain.daily_mission_masters.dto.response.DailyMissionResponseDTO;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.dto.response.UserResponse;
import com.example.cp_main_be.domain.user_daily_missions.service.UserDailyMissionService;
import com.example.cp_main_be.global.util.ApiResponse;
import com.google.protobuf.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mission")
@RequiredArgsConstructor
public class UserDailyMissionController {

    private final UserDailyMissionService userDailyMissionService;

    @GetMapping("/daily")
    @Operation(summary = "유저 일일 미션 목록 조회 or 할당된 미션이 없다면 할당 API ")
    public ResponseEntity<ApiResponse<DailyMissionResponseDTO>> getDailyMissions(@AuthenticationPrincipal User user) {
        DailyMissionResponseDTO response = userDailyMissionService.getDailyMissions(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/daily/{userDailyMissionId}/complete")
    @Operation(summary = "미션 완료 처리 API")
    public ResponseEntity<ApiResponse<Void>> completeDailyMission(@PathVariable Long userDailyMissionId) {
        userDailyMissionService.completeDailyMission(userDailyMissionId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }


}