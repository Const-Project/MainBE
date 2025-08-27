package com.example.cp_main_be.domain.mission.calendar;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.mission.MissionAggregationService;
import com.example.cp_main_be.domain.mission.calendar.dto.CalendarDayResponse;
import com.example.cp_main_be.domain.mission.calendar.dto.CalendarResponse;
import com.example.cp_main_be.domain.mission.user_daily_mission.dto.MissionCountPerDay;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

  private final UserRepository userRepository;
  // UserDailyMissionRepository -> MissionAggregationService로 변경
  private final MissionAggregationService missionAggregationService;

  public CalendarResponse getCalendarForMonth(UUID userUuid, int year, int month) {
    User user =
        userRepository
            .findByUuid(userUuid)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

    YearMonth yearMonth = YearMonth.of(year, month);
    LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
    LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

    // 2. Aggregation Service를 통해 한 번에 모든 미션 통계를 가져옴
    List<MissionCountPerDay> missionCounts =
        missionAggregationService.getMissionCountsPerDay(user, startDate, endDate);

    // 3. Map으로 변환 (이하 로직은 변경 없음)
    Map<Integer, Long> missionCountMap =
        missionCounts.stream()
            .collect(Collectors.toMap(MissionCountPerDay::getDay, MissionCountPerDay::getCount));

    // 4. CalendarDayResponse 생성
    List<CalendarDayResponse> days =
        IntStream.rangeClosed(1, yearMonth.lengthOfMonth())
            .mapToObj(
                day -> {
                  Long count = missionCountMap.getOrDefault(day, 0L);
                  return new CalendarDayResponse(day, Math.toIntExact(count));
                })
            .collect(Collectors.toList());

    return new CalendarResponse(year, month, days);
  }
}
