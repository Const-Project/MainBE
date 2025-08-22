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
  private Long id;

  @Column(nullable = false, unique = true)
  private String typeName; // 예: "스킨답서스", "산세베리아" (고유 식별값)

  @Column(nullable = false)
  private String defaultImageUrl; // 기본 이미지 URL

  private String description; // 식물에 대한 설명

  // TODO: 해금 레벨, 등급 등 아바타 종류에 대한 고정 정보 추가 가능
}
