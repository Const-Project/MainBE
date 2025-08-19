package com.example.cp_main_be.domain.daily_mission_masters.service;

import com.example.cp_main_be.domain.daily_mission_masters.MissionType;
import com.example.cp_main_be.domain.daily_mission_masters.domain.DailyMissionMasters;
import com.example.cp_main_be.domain.daily_mission_masters.domain.repository.DailyMissionMastersRepository;
import com.example.cp_main_be.domain.daily_mission_masters.dto.response.DailyMissionResponseDTO;
import com.example.cp_main_be.domain.user_daily_missions.service.UserDailyMissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

// Service 클래스를 DailyMissionService라고 가정합니다.
@ExtendWith(MockitoExtension.class)
class DailyMissionServiceTest {

    @InjectMocks
    private UserDailyMissionService dailyMissionService;

    @Mock
    private DailyMissionMastersRepository dailyMissionMastersRepository;

    @DisplayName("특정 사용자의 일일 미션 목록을 성공적으로 조회한다.")
    @Test
    void getDailyMissions_Success() {
        // given: 테스트 준비
        final Long userId = 1L;
        // Repository가 반환할 모의 데이터 생성 (DTO 구조에 맞게 completed 필드 제거)
        final List<DailyMissionMasters> mockMissions = Arrays.asList(
                DailyMissionMasters.builder()
                        .id(1L)
                        .missionType(MissionType.DIARY) // Enum 타입 필드 추가
                        .title("걷기 30분")
                        .description("공원에서 30분 이상 걷기")
                        .content("오늘 날씨도 좋으니 가볍게 산책해보세요!")
                        .rewardPoints(10L)
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .build(),
                DailyMissionMasters.builder()
                        .id(2L)
                        .missionType(MissionType.DIARY)
                        .title("물 2L 마시기")
                        .description("하루 동안 총 2L의 물 마시기")
                        .content("건강을 위한 작은 습관!")
                        .rewardPoints(15L)
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // dailyMissionMastersRepository.findAllById(userId) 호출 시 mockMissions를 반환하도록 설정
        given(dailyMissionMastersRepository.findAllById(Collections.singleton(userId))).willReturn(mockMissions);

        // when: 테스트 실행
        // 실제 DTO인 DailyMissionResponseDTO를 사용합니다.
        DailyMissionResponseDTO responseDTO = dailyMissionService.getDailyMissions(userId);

        // then: 결과 검증
        assertThat(responseDTO).isNotNull();
        // DTO의 필드명 'todayMissions'로 검증
        assertThat(responseDTO.getTodayMissions()).hasSize(2);

        // DTO의 내부 클래스 MissionSummaryDTO의 필드명에 맞춰 검증
        DailyMissionResponseDTO.MissionSummaryDTO firstMission = responseDTO.getTodayMissions().get(0);
        assertThat(firstMission.getMissionId()).isEqualTo(1L);
        assertThat(firstMission.getMissionTitle()).isEqualTo("걷기 30분");
        assertThat(firstMission.getMissionDescription()).isEqualTo("공원에서 30분 이상 걷기");

        DailyMissionResponseDTO.MissionSummaryDTO secondMission = responseDTO.getTodayMissions().get(1);
        assertThat(secondMission.getMissionId()).isEqualTo(2L);
        assertThat(secondMission.getMissionTitle()).isEqualTo("물 2L 마시기");

        // repository의 findAllById 메서드가 정확히 1번 호출되었는지 검증
        verify(dailyMissionMastersRepository).findAllById(Collections.singleton(userId));
    }

    @DisplayName("사용자의 일일 미션이 없는 경우 빈 목록을 반환한다.")
    @Test
    void getDailyMissions_EmptyList() {
        // given
        final Long userId = 2L;

        given(dailyMissionMastersRepository.findAllById(Collections.singleton(userId))).willReturn(Collections.emptyList());

        // when
        DailyMissionResponseDTO responseDTO = dailyMissionService.getDailyMissions(userId);

        // then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getTodayMissions()).isNotNull();
        assertThat(responseDTO.getTodayMissions()).isEmpty();

        verify(dailyMissionMastersRepository).findAllById(Collections.singleton(userId));
    }
}


