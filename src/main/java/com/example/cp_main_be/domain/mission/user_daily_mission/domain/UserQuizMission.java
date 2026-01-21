package com.example.cp_main_be.domain.mission.user_daily_mission.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DiscriminatorValue("QUIZ")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserQuizMission extends UserDailyMission {

  @Builder
  public UserQuizMission(User user, DailyMissionMaster dailyMissionMaster) {
    super(user, dailyMissionMaster);
  }

  public void setSelectedAnswerNumber(int optionOrder) {
    super.setSelectedAnswerNumber(optionOrder);
  }
}
