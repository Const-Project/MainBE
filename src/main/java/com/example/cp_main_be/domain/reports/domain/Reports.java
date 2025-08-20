package com.example.cp_main_be.domain.reports.domain;

import com.example.cp_main_be.domain.reports.enums.ReportStatus;
import com.example.cp_main_be.domain.reports.enums.TargetType;
import com.example.cp_main_be.domain.user.domain.User;
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
public class Reports {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "report_id")
  private Long id;

  @Column(name = "target_type")
  private TargetType targetType;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "report_reason_id")
  private ReportReason reason;

  @Column(name = "additional_comment")
  private String additionalComment;

  @Enumerated(EnumType.STRING)
  @Column(name = "report_status")
  private ReportStatus status;

  @Column(name = "created_at")
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(name = "reviewed_at")
  private LocalDateTime reviewedAt;

  @Column(name = "reviewer_id")
  private Long reviewerId;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "user_id")
  private User user;
}
