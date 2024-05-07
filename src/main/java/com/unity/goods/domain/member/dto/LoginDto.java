package com.unity.goods.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class LoginDto {

  @Getter
  @AllArgsConstructor
  @Builder
  public static class LoginRequest {

    private String email;
    private String password;
  }
}
