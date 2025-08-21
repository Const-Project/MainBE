package com.example.cp_main_be.domain.mission.calendar.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.calendar.dto.CalendarResponse;
import com.example.cp_main_be.domain.mission.user_daily_mission.service.CalendarService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
@Tag(name = "캘린더 API", description = "캘린더 관련 기능을 제공합니다.")
public class CalendarController {

  private final CalendarService calendarService;

  @Operation(summary = "캘린더 조회", description = "내 캘린더를 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<CalendarResponse>> getCalendar(
      @AuthenticationPrincipal User user,
      @RequestParam("y") int year,
      @RequestParam("m") int month) {

    CalendarResponse calendarData =
        calendarService.getCalendarForMonth(user.getUuid(), year, month);

    return ResponseEntity.ok(ApiResponse.success(calendarData));
  }
}
