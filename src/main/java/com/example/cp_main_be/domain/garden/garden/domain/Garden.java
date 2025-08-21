package com.example.cp_main_be.domain.garden.garden.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "garden")
public class Garden {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @JsonBackReference
  private User user;

  @Column(nullable = false)
  private Integer slotNumber;

  @Column(nullable = false)
  private Integer waterCount = 0;

  @Column(nullable = false)
  private Integer sunlightCount = 0;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate private LocalDateTime updatedAt;

  @Builder
  public Garden(User user, Integer slotNumber) {
    this.user = user;
    this.slotNumber = slotNumber;
    this.waterCount = 0;
    this.sunlightCount = 0;
  }

  public void increaseWaterCount() {
    this.waterCount++;
  }

  public void increaseSunlightCount() {
    this.sunlightCount++;
  }
}
