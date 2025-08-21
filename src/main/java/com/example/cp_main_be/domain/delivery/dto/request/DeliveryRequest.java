package com.example.cp_main_be.domain.delivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeliveryRequest {

  @NotBlank(message = "수령인 이름은 필수입니다.")
  private String recipientName;

  @NotBlank(message = "수령인 연락처는 필수입니다.")
  private String recipientPhone;

  @NotBlank(message = "우편번호는 필수입니다.")
  private String postalCode;

  @NotBlank(message = "주소는 필수입니다.")
  private String address;

  private String addressDetail;
  private String message;
}
