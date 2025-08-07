package com.example.cp_main_be.domain.user_daily_missions.domain.service;

import com.example.cp_main_be.domain.misson.domain.DailyMissionMasters;
import com.example.cp_main_be.domain.misson.domain.repository.DailyMissionMastersRepository;
import com.example.cp_main_be.domain.misson.dto.response.DailyMissionResponseDTO;
import com.example.cp_main_be.domain.user_daily_missions.domain.UserDailyMissions;
import com.example.cp_main_be.domain.user_daily_missions.domain.repository.UserDailyMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Transactional
public class UserDailyMissionService {

    private final UserDailyMissionRepository userDailyMissionRepository;
    private final DailyMissionMastersRepository dailyMissionMastersRepository;

    public DailyMissionResponseDTO getDailyMissions(Long userId) {
        List<UserDailyMissions> dailyMissions = userDailyMissionRepository.findAllByUserId(userId);
        List<DailyMissionMasters> dailyMissionMasters = dailyMissions.stream()
                .map(mission -> dailyMissionMastersRepository.findById(mission
                        .getDailyMissionMasters()
                        .getId())
                        .orElse(new DailyMissionMasters()))
                .toList();

        return DailyMissionResponseDTO.from(dailyMissionMasters);
    }

    public void completeDailyMission(Long dailyMissionId) {
        UserDailyMissions mission = userDailyMissionRepository.findById(dailyMissionId).orElse(null);
        if (mission == null) throw new RuntimeException("미션을 찾을 수 없습니다.");
        userDailyMissionRepository.delete(mission);
    }
}