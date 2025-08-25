package com.example.cp_main_be.domain.avatar.avatar.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvatarMaster {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 생성 말고 선택시 주어지는 전형적 선택지(아바타)는 10개 -> 스킨답서스,몬스테라 등 id는 0~9중 하나, db에 10개만 넣을거임

  @Column(nullable = false)
  private String defaultImageUrl; // 기본 이미지 URL

  private String description; // 식물에 대한 설명

  // TODO: 해금 레벨, 등급 등 아바타 종류에 대한 고정 정보 추가 가능
}
