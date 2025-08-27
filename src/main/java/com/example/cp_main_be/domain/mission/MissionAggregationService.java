package com.example.cp_main_be.domain.mission; // 패키지 위치는 프로젝트 구조에 맞게 조정하세요.

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.mission.user_daily_mission.dto.MissionCountPerDay;
import com.example.cp_main_be.domain.realquiz.repository.UserQuizRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionAggregationService {

  private final DiaryRepository diaryRepository;
  // 의존성 주입 변경
  private final UserQuizRepository userQuizRepository;

  // private final MindCheckRepository mindCheckRepository; // 추후 MindCheck 기능 추가 시 주입

  public List<MissionCountPerDay> getMissionCountsPerDay(
      User user, LocalDateTime startDate, LocalDateTime endDate) {

    // 1. 각 Repository에서 날짜별 완료 미션 개수 조회
    List<MissionCountPerDay> diaryCounts =
        diaryRepository.findCompletedCountsPerDay(user, startDate, endDate).stream()
            .peek(
                mission -> {
                  long currentCount = mission.getCount();
                  if (currentCount >= 3) {
                    mission.setCount(3L);
                  }
                })
            .toList();

    // userQuizRepository의 메서드 호출로 변경
    List<MissionCountPerDay> quizCounts =
        userQuizRepository.findCompletedCountsPerDay(user, startDate, endDate);
    // List<MissionCountPerDay> mindCheckCounts =
    // mindCheckRepository.findCompletedCountsPerDay(user, startDate, endDate);

    // 2. 모든 결과를 하나의 Stream으로 합친 후, 날짜(day)별로 그룹핑하여 count 합산
    // 이 부분은 변경할 필요 없이 그대로 동작합니다.
    Map<Integer, Long> combinedCounts =
        Stream.of(diaryCounts, quizCounts /*, mindCheckCounts */)
            .flatMap(List::stream)
            .collect(
                Collectors.groupingBy(
                    MissionCountPerDay::getDay,
                    Collectors.summingLong(MissionCountPerDay::getCount)));

    // 3. 합산된 Map 결과를 다시 List<MissionCountPerDay> 형태로 변환하여 반환
    return combinedCounts.entrySet().stream()
        .map(entry -> new MissionCountPerDay(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }
}
