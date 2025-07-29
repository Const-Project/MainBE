package com.example.cp_main_be.avatar.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Avatar {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "avatar_id")
  private Long id;

  @Column(name = "image_url")
  private String imageUrl;
}
