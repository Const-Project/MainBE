package com.example.cp_main_be.domain.admin.presentation;

// 만들 것
// GET /admin/users (사용자 목록 관리)
// PUT /admin/users/{userId}/status (사용자 상태 변경)
// GET /admin/reports (신고 목록 관리)
// PUT /admin/reports/{reportId} (신고 처리)
// DELETE /admin/contents/{type}/{id} (콘텐츠 삭제)
// POST /admin/missions/daily (일일 미션 생성)
// PUT /admin/missions/daily/{id} (일일 미션 수정)
// POST /admin/keywords (일일 키워드 등록)
// POST /admin/quiz (퀴즈 질문 등록)
// POST /admin/plants (새 식물 등록)
// PUT /admin/plants/{id} (식물 정보 수정)

import com.example.cp_main_be.domain.admin.dto.AdminRequestDTO;
import com.example.cp_main_be.domain.admin.dto.AdminResponseDTO;
import com.example.cp_main_be.domain.admin.service.AdminService;
import com.example.cp_main_be.domain.delivery.domain.DeliveryPlant;
import com.example.cp_main_be.domain.delivery.dto.request.DeliveryPlantRequest;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.daily_keywords.domain.DailyKeywords;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import com.example.cp_main_be.domain.mission.quiz.domain.QuizOptions;
import com.example.cp_main_be.domain.reports.domain.Reports;
import com.example.cp_main_be.domain.reports.enums.ReportStatus;
import com.example.cp_main_be.global.common.ApiResponse;
import com.example.cp_main_be.global.dto.ListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "어드민 API", description = "어드민 권한의 기능을 제공합니다.")
public class AdminController {

  private final AdminService adminService;

  @PostMapping("/missions/daily")
  @Operation(summary = "일일 미션(미션마스터) 생성 API")
  public ResponseEntity<ApiResponse<AdminResponseDTO.DailyMissionMastersResDTO>> createDailyMission(
      @RequestBody AdminRequestDTO.CreateMissionRequestDTO requestDTO) {
    DailyMissionMaster response = adminService.createDailyMissionMasters(requestDTO);
    AdminResponseDTO.DailyMissionMastersResDTO result =
        DailyMissionMaster.toDailyMissionMastersResDTO(response);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @PutMapping("/missions/daily/{dailyMissionMasterId}")
  @Operation(summary = "일일 미션(미션마스터) 수정 API")
  public ResponseEntity<ApiResponse<AdminResponseDTO.DailyMissionMastersResDTO>> updateDailyMission(
      @RequestBody AdminRequestDTO.UpdateMissionRequestDTO requestDTO, @PathVariable Long id) {
    DailyMissionMaster response = adminService.updateDailyMissionMasters(requestDTO, id);
    AdminResponseDTO.DailyMissionMastersResDTO result =
        DailyMissionMaster.toDailyMissionMastersResDTO(response);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @PostMapping("/keywords")
  @Operation(summary = "일일 키워드 등록 API")
  public ResponseEntity<ApiResponse<DailyKeywords>> createKeyword(
      @RequestBody AdminRequestDTO.CreateKeywordRequestDTO requestDTO) {
    DailyKeywords dailyKeywords = adminService.createDailyKeywords(requestDTO);
    return ResponseEntity.ok(ApiResponse.success(dailyKeywords));
  }

  @GetMapping("/users")
  @Operation(summary = "사용자 목록 조회 API")
  public ResponseEntity<ApiResponse<ListResponse<AdminResponseDTO.UserResDTO>>> getUsers() {
    List<User> users = adminService.getUsers();
    ListResponse<AdminResponseDTO.UserResDTO> listResponse =
        new ListResponse<>(
            users.stream()
                .map(
                    user ->
                        AdminResponseDTO.UserResDTO.builder()
                            .username(user.getNickname())
                            .uuid(user.getUuid())
                            .id(user.getId())
                            .build())
                .toList());
    return ResponseEntity.ok(ApiResponse.success(listResponse));
  }

  @PutMapping("/users/{userId}/status")
  @Operation(summary = "사용자 상태 변경(ban 등) API")
  public ResponseEntity<ApiResponse<User>> changeUserStatus(
      @PathVariable(name = "userId") Long userId,
      AdminRequestDTO.ChangeUserStatusRequestDTO request) {
    User user = adminService.changeUserStatus(userId, request);
    return ResponseEntity.ok(ApiResponse.success(user));
  }

  @PostMapping("/quiz")
  @Operation(summary = "퀴즈 선지 등록 API")
  public ResponseEntity<ApiResponse<QuizOptions>> createQuizOption(
      AdminRequestDTO.CreateQuizRequestDTO requestDTO) {
    QuizOptions quizOptions = adminService.createQuizOption(requestDTO);
    return ResponseEntity.ok(ApiResponse.success(quizOptions));
  }

  //
  //  @PostMapping("/avatar-master")
  //  @Operation(summary = "새 아바타 마스터 등록 API")
  //  public ResponseEntity<ApiResponse<AdminResponseDTO.PlantMasterResDTO>> createNewPlant(
  //      AdminRequestDTO.CreatePlantMasterRequestDTO requestDTO) {
  //    PlantMasters plantMaster = adminService.createNewPlant(requestDTO);
  //    AdminResponseDTO.PlantMasterResDTO result = PlantMasters.toPlantMasterResDTO(plantMaster);
  //    return ResponseEntity.ok(ApiResponse.success(result));
  //  }
  //
  //  @PutMapping("/plants/{avatar-master-id}")
  //  @Operation(summary = "아바타 마스터 정보 수정 API")
  //  public ResponseEntity<ApiResponse<AdminResponseDTO.PlantMasterResDTO>> updatePlantMasters(
  //      @PathVariable(name = "avatar-master-id") Long plantId,
  //      AdminRequestDTO.UpdatePlantMasterRequestDTO requestDTO) {
  //    PlantMasters plantMaster = adminService.updatePlantMasters(plantId, requestDTO);
  //    AdminResponseDTO.PlantMasterResDTO result = PlantMasters.toPlantMasterResDTO(plantMaster);
  //    return ResponseEntity.ok(ApiResponse.success(result));
  //  }

  @GetMapping("/reports")
  @Operation(summary = "신고 목록 조회 API")
  public ResponseEntity<ApiResponse<ListResponse<AdminResponseDTO.ReportResDTO>>> getAllReports() {
    List<Reports> reports = adminService.getAllReports();
    ListResponse<AdminResponseDTO.ReportResDTO> listResponse =
        new ListResponse<>(
            reports.stream()
                .map(
                    request ->
                        AdminResponseDTO.ReportResDTO.builder()
                            .reportReason(request.getReason())
                            .reportDate(request.getCreatedAt())
                            .reportId(request.getId())
                            .status(request.getStatus())
                            .reviewDate(request.getReviewedAt())
                            .reviewerId(request.getReviewerId())
                            .build())
                .toList());
    return ResponseEntity.ok(ApiResponse.success(listResponse));
  }

  @PutMapping("/reports/{reportId}")
  @Operation(summary = "신고 상태 변경 API")
  public ResponseEntity<ApiResponse<AdminResponseDTO.ReportResDTO>> updateReportStatus(
      @PathVariable(name = "reportId") Long reportId,
      @RequestParam(name = "status") ReportStatus reportStatus) {
    Reports result = adminService.updateReportStatus(reportId, reportStatus);
    AdminResponseDTO.ReportResDTO reportResDTO =
        AdminResponseDTO.ReportResDTO.builder()
            .reportReason(result.getReason())
            .status(result.getStatus())
            .reportDate(result.getCreatedAt())
            .reportId(result.getId())
            .reviewDate(result.getReviewedAt())
            .reviewerId(result.getReviewerId())
            .build();

    return ResponseEntity.ok(ApiResponse.success(reportResDTO));
  }

  @PostMapping(value = "/plant/delivery/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "택배로 받을 식물 추가")
  public ResponseEntity<ApiResponse<DeliveryPlant>> addDeliveryPlant(
      @RequestParam MultipartFile file, @RequestBody DeliveryPlantRequest request) {
    DeliveryPlant deliveryPlant = adminService.addDeliveryPlant(file, request);
    return ResponseEntity.ok(ApiResponse.success(deliveryPlant));
  }
}
