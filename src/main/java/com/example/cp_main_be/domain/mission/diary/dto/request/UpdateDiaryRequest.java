package com.example.cp_main_be.domain.mission.diary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDiaryRequest {

  @NotBlank(message = "제목은 필수 입력 항목입니다.")
  @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
  private String title;

  @NotBlank(message = "내용은 필수 입력 항목입니다.")
  private String content;

  private String imageUrl;

  @NotNull(message = "공개 여부는 필수값입니다. (true/false)")
  private Boolean isPublic;

  private Long imageId;
}
