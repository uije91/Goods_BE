package com.unity.goods.domain.email.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public class EmailVerificationCheckDto {

  @Getter
  public static class EmailVerificationCheckRequest {
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$",
        message = "이메일 형식에 맞지 않습니다.")
    private String email;

    @Size(min = 6, max = 6) // 6자리
    private String verificationNumber;
  }


}
