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
@DiscriminatorValue("IMAGE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserImageMission extends UserDailyMission {

  @Builder
  public UserImageMission(User user, DailyMissionMaster dailyMissionMaster) {
    super(user, dailyMissionMaster);
  }

  public void setSubmissionImageUrl(String imageUrl) {
    super.setSubmissionImageUrl(imageUrl);
  }
}
