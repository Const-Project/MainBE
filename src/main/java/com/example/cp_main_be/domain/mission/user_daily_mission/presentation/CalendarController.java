package com.example.cp_main_be.domain.mission.user_daily_mission.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.calendar.dto.CalendarResponse;
import com.example.cp_main_be.domain.mission.user_daily_mission.service.CalendarService;
import com.example.cp_main_be.global.common.ApiResponse;
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
public class CalendarController {

  private final CalendarService calendarService;

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
