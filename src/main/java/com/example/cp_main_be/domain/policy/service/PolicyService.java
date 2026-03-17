package com.example.cp_main_be.domain.policy.service;

import org.springframework.stereotype.Service;

@Service
public class PolicyService {

  public String getPolicyContent() {
    // 실제 약관 내용은 DB나 파일에서 가져오도록 구현
    return """
        제1조 (목적)
        이 약관은 나풀나풀이가 제공하는 반려식물 아바타 키우기 서비스(이하 "서비스")의 이용과 관련하여 회사와 회원 간의 권리, 의무 및 책임 사항을 규정함을 목적으로 합니다.

        제2조 (정의)
        "회원"이라 함은 본 약관에 동의하고 회사가 제공하는 서비스를 이용하는 자를 말합니다.
        "아바타 식물"이라 함은 회원이 앱 내에서 돌보고 성장시키는 가상의 식물을 의미합니다.
        "콘텐츠"라 함은 서비스 내에서 제공되는 이미지, 텍스트, 데이터 등을 말합니다.

        제3조 (약관의 효력 및 변경)
        본 약관은 회원이 동의함과 동시에 효력이 발생합니다.
        회사는 필요 시 관련 법령을 위배하지 않는 범위 내에서 약관을 변경할 수 있습니다.
        """;
  }
}
