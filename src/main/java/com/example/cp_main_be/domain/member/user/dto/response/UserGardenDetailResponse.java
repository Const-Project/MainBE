package com.example.cp_main_be.domain.member.user.dto.response;

// GardenResponse를
// 상속 또는 포함
import com.example.cp_main_be.domain.home.HomeResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

// 기존 GardenResponse 필드 포함 + @Builder로 추가 필드 설정
@Getter
@Setter
@Builder
public class UserGardenDetailResponse {
  private Long gardenId;
  private HomeResponseDto.AvatarInfo avatarInfo; // 해당 정원에 배치된 아바타의 상세 정보

  private Boolean isWateringAbleByMe; // 현재 접속 유저가 이 정원에 물을 줄 수 있는지 여부

  private Integer waterCount; // 👈 이 필드 추가
  private Integer sunlightCount; // 👈 이 필드 추가
}
