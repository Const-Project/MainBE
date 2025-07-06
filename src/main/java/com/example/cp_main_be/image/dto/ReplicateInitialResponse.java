package com.example.cp_main_be.image.dto;

// record를 사용하면 final 필드, 생성자, getter, equals, hashCode, toString을 자동으로 만들어줍니다.
public record ReplicateInitialResponse(String id, String status, Urls urls // String이 아닌 Urls 객체 타입
    ) {
  // 내부(nested) 레코드로 urls 객체를 표현
  public record Urls(
      String get, // JSON 필드명이 'get'
      String cancel) {}
}
