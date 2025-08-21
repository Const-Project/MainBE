package com.example.cp_main_be.domain.mission.user_daily_mission.domain;

import com.example.cp_main_be.domain.content.image.DailyMissionImage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DiscriminatorValue("IMAGE") // 부모 테이블의 mission_type 컬럼에 "IMAGE"로 저장됨
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserImageMission extends UserDailyMission {
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "image_id")
  private DailyMissionImage dailyMissionImage;

  // 생성자 등 필요한 로직 추가 ...
}
