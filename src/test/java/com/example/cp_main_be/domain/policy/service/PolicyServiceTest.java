// package com.example.cp_main_be.domain.policy.service;
//
// import org.junit.jupiter.api.Assertions;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.junit.jupiter.MockitoExtension;
//
// @ExtendWith(MockitoExtension.class)
// class PolicyServiceTest {
//
//  @InjectMocks private PolicyService policyService;
//
//  @DisplayName("약관 내용 조회 성공")
//  @Test
//  void getPolicyContent_success() {
//    // when
//    String policyContent = policyService.getPolicyContent();
//
//    // then
//    Assertions.assertNotNull(policyContent);
//    Assertions.assertTrue(policyContent.contains("이용 약관 내용입니다."));
//    Assertions.assertTrue(policyContent.contains("개인정보 처리 방침 내용입니다."));
//  }
// }
