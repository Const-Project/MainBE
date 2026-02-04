package com.example.cp_main_be.domain.mission.daily_keywords.domain;

import com.example.cp_main_be.domain.admin.dto.AdminRequestDTO;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
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
public class DailyKeywords {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "keyword_id")
  private Long id;

  @Column(name = "keyword_date")
  private LocalDateTime keywordDate;

  @Column(name = "keyword")
  private String keyword;

  @Column(name = "created_at")
  @CreationTimestamp
  private LocalDateTime createdAt;

  public void from(AdminRequestDTO.CreateKeywordRequestDTO requestDTO) {
    if (requestDTO.getKeyword() != null && requestDTO.getKeywordDate() != null) {
      this.keyword = requestDTO.getKeyword();
      this.keywordDate = requestDTO.getKeywordDate();
    } else throw new CustomApiException(ErrorCode.INVALID_REQUEST, "누락된 속성이 존재합니다.");
  }
}
