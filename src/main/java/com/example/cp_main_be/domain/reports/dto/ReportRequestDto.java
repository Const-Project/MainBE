package com.example.cp_main_be.domain.reports.dto;

import com.example.cp_main_be.domain.reports.enums.TargetType;
import lombok.Getter;

@Getter
public class ReportRequestDto {
  private TargetType targetType;
  private Long targetId;
  private String reason;
  private String additionalComment;
}
