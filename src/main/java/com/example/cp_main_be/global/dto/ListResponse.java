package com.example.cp_main_be.global.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

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
