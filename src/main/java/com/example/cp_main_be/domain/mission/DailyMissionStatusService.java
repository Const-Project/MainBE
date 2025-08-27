// DailyMissionStatusService.java (새로 생성)
package com.example.cp_main_be.domain.mission;

import com.example.cp_main_be.domain.mission.daily_mission_master.domain.repository.DailyMissionMastersRepository;
import com.example.cp_main_be.domain.mission.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.realquiz.repository.UserQuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyMissionStatusService {

  private final DailyMissionMastersRepository dailyMissionMastersRepository;
  private final DiaryRepository diaryRepository;
  private final UserQuizRepository userQuizRepository;
  // private final SurveyRepository surveyRepository; // 나중에 추가

  //    public List<MissionStatusDto> getDailyMissionStatus(User user) {
  //        // 1. 오늘 날짜에 해당하는 미션 마스터 목록을 가져옵니다.
  //        // (DailyMissionMaster에 날짜 필드가 있다는 가정 하에, 없다면 생성 로직에 따라 조회)
  //        List<DailyMissionMaster> todayMissions =
  // dailyMissionMastersRepository.findTodayMissions(); // 이 메서드는 새로 만들어야 합니다.
  //
  //        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
  //        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
  //
  //        // 2. 각 미션의 완료 여부를 확인하여 DTO 리스트를 생성합니다.
  //        return todayMissions.stream()
  //                .map(mission -> {
  //                    boolean isCompleted = checkMissionCompletion(user, mission, startOfDay,
  // endOfDay);
  //                    return MissionStatusDto.builder()
  //                            .title(mission.getTitle())
  //                            .missionType(mission.getMissionType())
  //                            .completed(isCompleted)
  //                            .build();
  //                })
  //                .collect(Collectors.toList());
  //    }
  //
  //    private boolean checkMissionCompletion(User user, DailyMissionMaster mission, LocalDateTime
  // start, LocalDateTime end) {
  //        switch (mission.getMissionType()) {
  //            case DIARY:
  //                // 오늘 작성한 일기가 하나라도 있으면 완료
  //                return !diaryRepository.findTodayDiaryByUser(user, start, end).isEmpty();
  //            case QUIZ:
  //                // 오늘 푼 퀴즈 중 완료(isCompleted=true)된 것이 있으면 완료
  //                return userQuizRepository.findAllTodayUserQuizByUser(user, start, end)
  //                        .stream().anyMatch(UserQuiz::getIsCompleted);
  //            case SURVEY:
  //                // return surveyRepository.findTodaySurveyByUser(user, start, end).isPresent();
  // // 예시
  //            default:
  //                return false;
  //        }
  //    }
}
