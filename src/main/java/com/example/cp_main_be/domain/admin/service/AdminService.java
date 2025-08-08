package com.example.cp_main_be.domain.admin.service;

import com.example.cp_main_be.daily_keywords.domain.DailyKeywords;
import com.example.cp_main_be.daily_keywords.domain.repository.DailyKeywordsRepository;
import com.example.cp_main_be.domain.admin.dto.AdminRequestDTO;
import com.example.cp_main_be.domain.daily_mission_masters.domain.DailyMissionMasters;
import com.example.cp_main_be.domain.daily_mission_masters.domain.repository.DailyMissionMastersRepository;
import com.example.cp_main_be.domain.quiz.domain.QuizOptions;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final DailyMissionMastersRepository dailyMissionMastersRepository;
    private final DailyKeywordsRepository dailyKeywordsRepository;

    private final UserService userService;

    public DailyMissionMasters createDailyMissionMasters(AdminRequestDTO.CreateMissionRequestDTO requestDTO) {
        DailyMissionMasters dailyMissionMasters = DailyMissionMasters.builder()
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .content(requestDTO.getContent())
                .missionType(requestDTO.getMissionType())
                .rewardPoints(requestDTO.getRewardPoints())
                .build();

        return dailyMissionMastersRepository.save(dailyMissionMasters);
    }

    public DailyMissionMasters updateDailyMissionMasters(AdminRequestDTO.UpdateMissionRequestDTO requestDTO, Long id) {

        DailyMissionMasters dailyMissionMasters = dailyMissionMastersRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 미션을 찾을 수 없습니다."));

        // null 인 컬럼들은 수정 안한다.
        dailyMissionMasters.update(requestDTO);
        return dailyMissionMasters;
    }

    public DailyKeywords createDailyKeywords(AdminRequestDTO.CreateKeywordRequestDTO requestDTO) {

        DailyKeywords dailyKeyword = new DailyKeywords();
        dailyKeyword.from(requestDTO);
        return dailyKeywordsRepository.save(dailyKeyword);
    }


    public List<User> getUsers()
    {
        return userService.findAllUsers();
    }

    public User chageUserStatus(Long userId, AdminRequestDTO.ChangeUserStatusRequestDTO requestDTO) {
        User user = userService.findUserById(userId);
        user.setStatus(requestDTO.getUserStatus());
        return user;
    }

    public QuizOptions createQuizOption(AdminRequestDTO.CreateQuizRequestDTO requestDTO) {
        DailyMissionMasters dailyMissionMaster = dailyMissionMastersRepository.findById(requestDTO.getMissionMasterId())
                .orElseThrow(() -> new IllegalStateException("미션 ID에 해당하는 미션이 존재하지 않습니다."));

        return QuizOptions.builder()        // QuizOptions 퀴즈의 선지
                .optionText(requestDTO.getOptionText())
                .optionOrder(requestDTO.getOptionOrder())
                .isCorrect(requestDTO.isCorrect())
                .dailyMissionMasters(dailyMissionMaster)
                .build();
    }
}
