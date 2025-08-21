package com.example.cp_main_be.domain.reports.domain;

import com.example.cp_main_be.domain.reports.enums.ReasonType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportReason {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "reason_type")
  private ReasonType reasonType;

  @Column(name = "reason_text")
  private String reasonText;

  @Column(name = "is_active")
  private boolean isActive;

  @Column(name = "created_at")
  @CreationTimestamp
  private LocalDateTime createdAt;
}
