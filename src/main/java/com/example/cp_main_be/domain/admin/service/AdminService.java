package com.example.cp_main_be.domain.admin.service;

import com.example.cp_main_be.domain.admin.dto.AdminRequestDTO;
import com.example.cp_main_be.domain.daily_mission_masters.domain.DailyMissionMasters;
import com.example.cp_main_be.domain.daily_mission_masters.domain.repository.DailyMissionMastersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final DailyMissionMastersRepository dailyMissionMastersRepository;
    public DailyMissionMasters createDailyMissionMasters(AdminRequestDTO.CreateRequestDTO requestDTO) {
        DailyMissionMasters dailyMissionMasters = DailyMissionMasters.builder()
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .content(requestDTO.getContent())
                .missionType(requestDTO.getMissionType())
                .rewardPoints(requestDTO.getRewardPoints())
                .build();
        dailyMissionMastersRepository.save(dailyMissionMasters);
        return dailyMissionMasters;
    }

    public DailyMissionMasters updateDailyMissionMasters(AdminRequestDTO.UpdateRequestDTO requestDTO, Long id) {

        DailyMissionMasters dailyMissionMasters = dailyMissionMastersRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 미션을 찾을 수 없습니다."));

        // null 인 컬럼들은 수정 안한다.
        dailyMissionMasters.update(requestDTO);
        return dailyMissionMasters;
    }
}
