package com.example.cp_main_be.domain.misson.presentation;


import com.example.cp_main_be.domain.misson.dto.response.DailyMissionResponseDTO;
import com.example.cp_main_be.domain.misson.service.DailyMissionService;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.global.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mission")
@RequiredArgsConstructor
public class DailyMissionController {

    private final DailyMissionService dailyMissionService;

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<DailyMissionResponseDTO>> getDailyMissions(@AuthenticationPrincipal User user) {
        DailyMissionResponseDTO response = dailyMissionService.getDailyMissions(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }



}
