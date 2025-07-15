package com.example.cp_main_be.image.dto;

import java.util.List;
import java.util.Map;

public record ReplicateStatusResponse(
    String id,
    String status, // "starting", "processing", "succeeded", "failed" 등
    List<String> output, // 성공 시 결과 이미지 URL 리스트
    String error, // 실패 시 에러 메시지
    Map<String, Object> metrics) {}
