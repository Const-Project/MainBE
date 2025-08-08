package com.example.cp_main_be.domain.admin.presentation;


//만들 것
//GET /admin/users (사용자 목록 관리)
//PUT /admin/users/{userId}/status (사용자 상태 변경)
//GET /admin/reports (신고 목록 관리)
//PUT /admin/reports/{reportId} (신고 처리)
//DELETE /admin/contents/{type}/{id} (콘텐츠 삭제)
//POST /admin/missions/daily (일일 미션 생성)
//PUT /admin/missions/daily/{id} (일일 미션 수정)
//POST /admin/keywords (일일 키워드 등록)
//POST /admin/quiz (퀴즈 질문 등록)
//POST /admin/plants (새 식물 등록)
//PUT /admin/plants/{id} (식물 정보 수정)

import com.example.cp_main_be.domain.admin.dto.AdminRequestDTO;
import com.example.cp_main_be.domain.admin.service.AdminService;
import com.example.cp_main_be.domain.daily_mission_masters.domain.DailyMissionMasters;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.global.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/missions/daily")
    @Operation(summary = "일일 미션(미션마스터) 생성 API")
    public ResponseEntity<ApiResponse<DailyMissionMasters>> createDailyMission(@RequestBody AdminRequestDTO.CreateRequestDTO requestDTO) {
        DailyMissionMasters response = adminService.createDailyMissionMasters(requestDTO);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("missions/daily/{id}")
    @Operation(summary = "일일 미션(미션마스터) 수정 API")
    public ResponseEntity<ApiResponse<DailyMissionMasters>> updateDailyMission(@RequestBody AdminRequestDTO.UpdateRequestDTO requestDTO, Long id) {
        DailyMissionMasters response = adminService.updateDailyMissionMasters(requestDTO, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
