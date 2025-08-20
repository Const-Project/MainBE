package com.example.cp_main_be.global.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ListResponse<T> {
  private List<T> content;

  // 나중에 추가될 수 있는 메타데이터
  // private int totalPages;
  // private long totalElements;

  public ListResponse(List<T> content) {
    this.content = content;
  }
}
