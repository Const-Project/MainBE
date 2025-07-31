package com.example.cp_main_be.domain.policy.service;

import org.springframework.stereotype.Service;

@Service
public class PolicyService {

  public String getPolicyContent() {
    // 실제 약관 내용은 DB나 파일에서 가져오도록 구현
    return "이용 약관 내용입니다.\n개인정보 처리 방침 내용입니다.";
  }
}
