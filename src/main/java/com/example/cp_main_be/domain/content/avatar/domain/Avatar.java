package com.example.cp_main_be.domain.content.avatar.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Avatar {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "avatar_id")
  private Long id;

  private String name;

  @Column(name = "image_url")
  private String imageUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id") // 외래 키 컬럼 지정
  private User user; // 특정 유저가 생성한 아바타인 경우 해당 유저, 기본 아바타인 경우 null

  @Column(name = "is_default_avatar") // 기본 아바타 여부를 나타내는 필드 추가
  @Builder.Default
  private boolean isDefaultAvatar = false; // 기본값은 false (사용자 생성 아바타)
}
