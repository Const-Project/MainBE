package com.example.cp_main_be.domain.member.auth.domain;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

  /** 토큰 문자열 자체를 PK로 사용 (필요 시 별도 ID를 둘 수도 있음) */
  @Id
  @Column(length = 512)
  private String token;

  /** 어떤 유저(UUID)의 토큰인지 매핑 */
  @Column(nullable = false)
  private UUID userUuid;

  /** 토큰 만료 시각 (서버가 인지하는 만료) */
  @Column(nullable = false)
  private LocalDateTime expiresAt;

  /** 선택: 동시 기기 구분용 (원치 않으면 제거) */
  private String deviceId; // X-Client-Device-Id 헤더 등으로 구분 가능
}
