package com.example.cp_main_be.domain.misson.service;

import com.example.cp_main_be.domain.misson.domain.DailyMissionMasters;
import com.example.cp_main_be.domain.misson.domain.repository.DailyMissionMastersRepository;
import com.example.cp_main_be.domain.misson.dto.response.DailyMissionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class DailyMissionService {

    private final DailyMissionMastersRepository dailyMissionMastersRepository;

    public DailyMissionResponseDTO getDailyMissions(Long userId) {
        List<DailyMissionMasters> dailyMissions = dailyMissionMastersRepository.findAllById(userId);
        return DailyMissionResponseDTO.from(dailyMissions);
    }
}
