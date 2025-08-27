package com.example.cp_main_be.domain.member.user.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserProfileResponse {
  private Long id;
  private String userNickname; // 프로필 주인의 닉네임
  private String profileImageUrl; // 프로필 주인의 이미지 URL 추가

  // 현재 접속 유저가 프로필 주인을 팔로우하는 상태
  // 0: 팔로우 안 함 (친구 추가 버튼 보임)
  // 1: 팔로우 중 (팔로우 취소 버튼 보임)
  // 2: 나를 팔로우 중 (맞팔로우 버튼 보임) - 이 상태는 isFriend가 false일 때만 의미
  private FollowStatus followStatus;

  //  private int profileUserLevel; // 프로필 주인의 레벨 추가
  private Long leftWaterCountForOthers; // 오늘 남에게 물을 줄 수 있는 남은 횟수 (현재 접속 유저 기준)

  // 프로필 주인의 모든 정원 목록. 각 정원마다 물주기 가능 여부 포함
  private List<UserGardenDetailResponse> userGardens; // GardenResponse 대신 상세 정보를 담는 새로운 DTO
}
