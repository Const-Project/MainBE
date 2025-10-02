package com.example.cp_main_be.domain.avatar.avatar.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
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

  @Size(max = 14, message = "닉네임은 14자 이하여야 합니다.")
  @Column(nullable = false)
  private String nickname;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "master_id", nullable = false)
  private AvatarMaster avatarMaster;

  @Column(name = "image_url")
  private String imageUrl;
}
