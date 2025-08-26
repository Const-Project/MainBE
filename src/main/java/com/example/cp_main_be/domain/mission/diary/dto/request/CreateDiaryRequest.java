package com.example.cp_main_be.domain.mission.diary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // JSON 역직렬화를 위해 기본 생성자가 필요합니다.
@AllArgsConstructor
public class CreateDiaryRequest {

  @NotBlank(message = "제목은 필수 입력 항목입니다.")
  @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
  private String title;

  @NotBlank(message = "내용은 필수 입력 항목입니다.")
  private String content;

  // 이미지는 보통 S3 같은 곳에 먼저 업로드한 뒤, 그 URL을 받아와 저장합니다.
  private String imageUrl;

  // 공개 여부는 선택사항으로, 값을 보내지 않으면 엔티티의 기본값(true)을 따릅니다.
  @NotNull(message = "공개 여부는 필수값입니다. (true/false)")
  private Boolean isPublic;

  private Long imageId; // 있으면 받고 없으면 안받음
}
